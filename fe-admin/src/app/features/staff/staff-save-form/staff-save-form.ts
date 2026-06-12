import {Component, inject, OnDestroy, OnInit, output, signal} from '@angular/core';
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {DatePicker} from "primeng/datepicker";
import {Select} from "primeng/select";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {MessageService} from "primeng/api";
import {Checkbox} from "primeng/checkbox";
import {StaffService} from "../../../core/service/staff.service";
import {CreateStaffRequest} from "../../../core/model/request/create-staff-request";
import {RoleService} from "../../../core/service/role.service";
import {RoleResponse} from "../../../core/model/response/role-response";
import {
    DayOfWeekValue,
    StaffWorkingSchedulePayload,
} from '../../../core/model/staff-working-schedule';

interface WorkingScheduleRow extends StaffWorkingSchedulePayload {
    label: string;
}

const WORK_START_MINUTES = 8 * 60 + 30;
const WORK_END_MINUTES = 20 * 60;
const SLOT_MINUTES = 30;
const DEFAULT_START_TIME = '08:30';
const DEFAULT_END_TIME = '20:00';

@Component({
    selector: 'app-staff-save-form',
    imports: [Button, InputText, Card, DatePicker, Select, Checkbox, ReactiveFormsModule],
    templateUrl: './staff-save-form.html',
    styleUrl: './staff-save-form.css',
})
export class StaffSaveForm implements OnInit, OnDestroy {
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
    roleLoading = signal(false);
    roleOptions = signal<{ label: string; value: string }[]>([]);

    readonly dayOptions: { label: string; value: DayOfWeekValue }[] = [
        {label: 'Thứ 2', value: 'MONDAY'},
        {label: 'Thứ 3', value: 'TUESDAY'},
        {label: 'Thứ 4', value: 'WEDNESDAY'},
        {label: 'Thứ 5', value: 'THURSDAY'},
        {label: 'Thứ 6', value: 'FRIDAY'},
        {label: 'Thứ 7', value: 'SATURDAY'},
        {label: 'Chủ nhật', value: 'SUNDAY'},
    ];
    workingSchedules = signal<WorkingScheduleRow[]>(this.createDefaultWorkingSchedules());
    readonly timeOptions = this.createTimeOptions();

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
        {label: 'Đang làm việc', value: true},
        {label: 'Đã nghỉ', value: false},
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
        const invalidSchedule = this.workingSchedules().some(row =>
            row.working && !this.isScheduleTimeRangeValid(row.startTime, row.endTime));
        if (invalidSchedule) {
            errs['workingSchedules'] = 'Giờ làm việc phải trong khoảng 08:30-20:00, bắt đầu nhỏ hơn kết thúc và cách nhau ít nhất 30 phút';
        }

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
            workingSchedules: this.toWorkingSchedulePayloads(),
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

    updateScheduleWorking(dayOfWeek: DayOfWeekValue, working: boolean): void {
        this.clearError('workingSchedules');
        this.patchWorkingSchedule(dayOfWeek, {working});
    }

    updateScheduleTime(dayOfWeek: DayOfWeekValue, field: 'startTime' | 'endTime', value: string): void {
        this.clearError('workingSchedules');
        const time = this.normalizeScheduleTime(value);
        this.patchWorkingSchedule(dayOfWeek, field === 'startTime' ? {startTime: time} : {endTime: time});
    }

    private patchWorkingSchedule(dayOfWeek: DayOfWeekValue, patch: Partial<WorkingScheduleRow>): void {
        this.workingSchedules.update(rows => rows.map(row =>
            row.dayOfWeek === dayOfWeek ? {...row, ...patch} : row));
    }

    private createDefaultWorkingSchedules(): WorkingScheduleRow[] {
        return this.dayOptions.map(day => ({
            dayOfWeek: day.value,
            label: day.label,
            working: true,
            startTime: DEFAULT_START_TIME,
            endTime: DEFAULT_END_TIME,
        }));
    }

    private createTimeOptions(): string[] {
        const options: string[] = [];
        for (let minutes = WORK_START_MINUTES; minutes <= WORK_END_MINUTES; minutes += SLOT_MINUTES) {
            const hour = `${Math.floor(minutes / 60)}`.padStart(2, '0');
            const minute = `${minutes % 60}`.padStart(2, '0');
            options.push(`${hour}:${minute}`);
        }
        return options;
    }

    private isScheduleTimeRangeValid(startTime: string, endTime: string): boolean {
        const startMinutes = this.toMinutes(startTime);
        const endMinutes = this.toMinutes(endTime);
        return startMinutes !== null
            && endMinutes !== null
            && startMinutes >= WORK_START_MINUTES
            && endMinutes <= WORK_END_MINUTES
            && startMinutes % SLOT_MINUTES === 0
            && endMinutes % SLOT_MINUTES === 0
            && startMinutes < endMinutes
            && endMinutes - startMinutes >= SLOT_MINUTES;
    }

    private normalizeScheduleTime(time: string | null | undefined): string {
        if (!time) return '';
        const [hour = '', minute = ''] = time.split(':');
        return hour && minute ? `${hour.padStart(2, '0')}:${minute.padStart(2, '0')}` : '';
    }

    private toMinutes(time: string | null | undefined): number | null {
        const normalizedTime = this.normalizeScheduleTime(time);
        if (!normalizedTime) return null;
        const [hour, minute] = normalizedTime.split(':').map(Number);
        if (!Number.isInteger(hour) || !Number.isInteger(minute)) return null;
        return hour * 60 + minute;
    }

    private toWorkingSchedulePayloads(): StaffWorkingSchedulePayload[] {
        return this.workingSchedules().map(({label, ...schedule}) => ({
            ...schedule,
            startTime: this.normalizeScheduleTime(schedule.startTime),
            endTime: this.normalizeScheduleTime(schedule.endTime),
        }));
    }
}
