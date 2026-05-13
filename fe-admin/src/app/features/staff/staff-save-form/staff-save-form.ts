import {Component, inject, OnDestroy, OnInit, output, signal} from '@angular/core';
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {DatePicker} from "primeng/datepicker";
import {Select} from "primeng/select";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MessageService} from "primeng/api";
import {Checkbox} from "primeng/checkbox";
import {StaffService} from "../../../core/service/staff.service";
import {CreateStaffRequest, WorkingScheduleRequest} from "../../../core/model/request/create-staff-request";
import {RoleService} from "../../../core/service/role.service";
import {RoleResponse} from "../../../core/model/response/role-response";

interface ScheduleRow {
    dayOfWeek: string;
    label: string;
    isWorking: boolean;
    startTime: string;
    endTime: string;
}

@Component({
    selector: 'app-staff-save-form',
    imports: [Button, InputText, Card, DatePicker, Select, Checkbox, FormsModule, ReactiveFormsModule],
    templateUrl: './staff-save-form.html',
    styleUrl: './staff-save-form.css',
})
export class StaffSaveForm implements OnInit, OnDestroy {
    readonly minWorkingTime = '07:00';
    readonly maxWorkingTime = '20:00';
    readonly workingTimeOptions = this.createWorkingTimeOptions();

    private readonly staffService = inject(StaffService);
    private readonly roleService = inject(RoleService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);
    private previewObjectUrl: string | null = null;

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    selectedFile = signal<File | null>(null);
    previewUrl = signal<string | null>(null);
    schedules = signal<ScheduleRow[]>([]);
    roleLoading = signal(false);
    roleOptions = signal<{ label: string; value: string }[]>([]);

    genderOptions = [
        {label: 'Nam', value: true},
        {label: 'Nữ', value: false},
    ];

    staffTypeOptions = [
        {label: 'Nha sĩ', value: 'DENTIST'},
        {label: 'Lễ tân', value: 'RECEPTIONIST'},
        {label: 'Y tá', value: 'NURSE'},
        {label: 'Quản lý', value: 'ADMIN'},
    ];

    activeOptions = [
        {label: 'Hoạt động', value: true},
        {label: 'Đã ẩn', value: false},
    ];

    readonly dayOptions = [
        {label: 'Thứ 2', value: 'MONDAY'},
        {label: 'Thứ 3', value: 'TUESDAY'},
        {label: 'Thứ 4', value: 'WEDNESDAY'},
        {label: 'Thứ 5', value: 'THURSDAY'},
        {label: 'Thứ 6', value: 'FRIDAY'},
        {label: 'Thứ 7', value: 'SATURDAY'},
        {label: 'Chủ nhật', value: 'SUNDAY'},
    ];

    saveForm = this.fb.group({
        code: this.fb.nonNullable.control(''),
        fullName: this.fb.nonNullable.control(''),
        dob: this.fb.control<Date | null>(null),
        gender: this.fb.control<boolean | null>(null),
        phone: this.fb.nonNullable.control(''),
        email: this.fb.nonNullable.control(''),
        address: this.fb.nonNullable.control(''),
        staffType: this.fb.control<string | null>(null),
        isActive: this.fb.nonNullable.control(true),
        createUser: this.fb.nonNullable.control(false),
        roleCode: this.fb.control<string | null>(null),
    });

    ngOnInit(): void {
        this.schedules.set(this.createDefaultSchedules());
        if (this.saveForm.controls.createUser.value) {
            this.loadRoles();
        }
    }

    ngOnDestroy(): void {
        this.revokePreviewUrl();
    }

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    toggleSchedule(dayOfWeek: string, isWorking: boolean): void {
        this.schedules.update(rows => rows.map(row => row.dayOfWeek === dayOfWeek ? {...row, isWorking} : row));
        this.clearError('workingSchedules');
    }

    updateScheduleTime(dayOfWeek: string, field: 'startTime' | 'endTime', value: string): void {
        this.schedules.update(rows => rows.map(row => row.dayOfWeek === dayOfWeek ? {...row, [field]: value} : row));
        this.clearError('workingSchedules');
    }

    onCreateUserChange(checked: boolean): void {
        this.clearError('roleCode');
        if (checked) {
            this.loadRoles();
            return;
        }

        this.saveForm.controls.roleCode.setValue(null);
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        const file = input.files?.[0] ?? null;
        this.revokePreviewUrl();
        this.selectedFile.set(file);
        if (file) {
            this.previewObjectUrl = URL.createObjectURL(file);
            this.previewUrl.set(this.previewObjectUrl);
        } else {
            this.previewUrl.set(null);
        }
    }

    private validate(): boolean {
        const val = this.saveForm.getRawValue();
        const errs: Record<string, string> = {};

        if (!val.fullName.trim()) errs['fullName'] = 'Vui lòng nhập họ và tên';
        if (!val.dob) errs['dob'] = 'Vui lòng chọn ngày sinh';
        if (val.gender === null) errs['gender'] = 'Vui lòng chọn giới tính';
        if (!val.phone.trim()) errs['phone'] = 'Vui lòng nhập số điện thoại';
        if (val.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.email)) errs['email'] = 'Email không hợp lệ';
        if (!val.staffType) errs['staffType'] = 'Vui lòng chọn chức vụ';
        if (val.createUser && !val.roleCode) errs['roleCode'] = 'Vui lòng chọn vai trò';

        const schedules = this.buildWorkingSchedules();
        const hasMissingTime = this.schedules().some(row => row.isWorking && (!row.startTime || !row.endTime));
        const outOfWorkingRange = schedules.some(row =>
            row.startTime < this.minWorkingTime ||
            row.startTime > this.maxWorkingTime ||
            row.endTime < this.minWorkingTime ||
            row.endTime > this.maxWorkingTime
        );
        const invalidTime = schedules.some(row => row.startTime >= row.endTime);

        if (hasMissingTime) errs['workingSchedules'] = 'Vui lòng nhập đủ giờ bắt đầu và giờ kết thúc cho ngày làm việc';
        else if (outOfWorkingRange) errs['workingSchedules'] = 'Thời gian làm việc chỉ được chọn từ 07:00 đến 20:00';
        else if (invalidTime) errs['workingSchedules'] = 'Giờ bắt đầu phải nhỏ hơn giờ kết thúc';

        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;

        const val = this.saveForm.getRawValue();
        const request: CreateStaffRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: this.formatLocalDate(val.dob),
            gender: val.gender,
            phone: val.phone,
            email: val.email,
            address: val.address,
            staffType: val.staffType!,
            isActive: val.isActive,
            roleCode: val.createUser ? val.roleCode : null,
            workingSchedules: this.buildWorkingSchedules(),
        };

        this.loading.set(true);
        this.staffService.create(request, this.selectedFile()).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã thêm nhân viên thành công'});
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể thêm nhân viên, vui lòng thử lại'});
            }
        });
    }

    private buildWorkingSchedules(): WorkingScheduleRequest[] {
        return this.schedules()
            .filter(row => row.isWorking && row.startTime && row.endTime)
            .map(row => ({
                dayOfWeek: row.dayOfWeek,
                startTime: row.startTime,
                endTime: row.endTime,
            }));
    }

    private createDefaultSchedules(): ScheduleRow[] {
        return this.dayOptions.map(day => ({
            dayOfWeek: day.value,
            label: day.label,
            isWorking: true,
            startTime: '08:00',
            endTime: '17:00',
        }));
    }

    private createWorkingTimeOptions(): { label: string; value: string }[] {
        const options: { label: string; value: string }[] = [];
        for (let hour = 7; hour <= 20; hour++) {
            const value = `${hour}`.padStart(2, '0') + ':00';
            options.push({label: value, value});
        }
        return options;
    }

    private loadRoles(): void {
        if (this.roleOptions().length > 0 || this.roleLoading()) return;

        this.roleLoading.set(true);
        this.roleService.findAssignable().subscribe({
            next: res => {
                this.roleOptions.set(res.data.map((role: RoleResponse) => ({
                    label: role.name,
                    value: role.code,
                })));
                this.roleLoading.set(false);
            },
            error: () => {
                this.roleLoading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách vai trò'});
            }
        });
    }

    private formatLocalDate(value: Date | null): string | null {
        if (!value) return null;
        const year = value.getFullYear();
        const month = `${value.getMonth() + 1}`.padStart(2, '0');
        const day = `${value.getDate()}`.padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    private revokePreviewUrl(): void {
        if (this.previewObjectUrl) {
            URL.revokeObjectURL(this.previewObjectUrl);
            this.previewObjectUrl = null;
        }
    }
}
