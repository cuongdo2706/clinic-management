import {Component, inject, input, OnDestroy, OnInit, output, signal} from '@angular/core';
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {DatePicker} from "primeng/datepicker";
import {Select} from "primeng/select";
import {ProgressSpinner} from "primeng/progressspinner";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MessageService} from "primeng/api";
import {StaffService} from "../../../core/service/staff.service";
import {UpdateStaffRequest} from "../../../core/model/request/update-staff-request";
import {WorkingScheduleRequest} from "../../../core/model/request/create-staff-request";
import {ENV} from "../../../environment";

interface ScheduleRow {
    uid: number;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
}

@Component({
    selector: 'app-staff-update-form',
    imports: [Button, InputText, Card, DatePicker, Select, ProgressSpinner, FormsModule, ReactiveFormsModule],
    templateUrl: './staff-update-form.html',
    styleUrl: './staff-update-form.css',
})
export class StaffUpdateForm implements OnInit, OnDestroy {
    private readonly staffService = inject(StaffService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);
    private previewObjectUrl: string | null = null;
    private nextScheduleUid = 1;
    private staffVersion = 0;
    private currentAvatarUrl: string | null = null;

    staff = input.required<number>();

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);
    errors = signal<Record<string, string>>({});
    selectedFile = signal<File | null>(null);
    previewUrl = signal<string | null>(null);
    schedules = signal<ScheduleRow[]>([]);

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

    dayOptions = [
        {label: 'Thứ 2', value: 'MONDAY'},
        {label: 'Thứ 3', value: 'TUESDAY'},
        {label: 'Thứ 4', value: 'WEDNESDAY'},
        {label: 'Thứ 5', value: 'THURSDAY'},
        {label: 'Thứ 6', value: 'FRIDAY'},
        {label: 'Thứ 7', value: 'SATURDAY'},
        {label: 'Chủ nhật', value: 'SUNDAY'},
    ];

    updateForm = this.fb.group({
        code: this.fb.nonNullable.control(''),
        fullName: this.fb.nonNullable.control(''),
        dob: this.fb.control<Date | null>(null),
        gender: this.fb.control<boolean | null>(null),
        phone: this.fb.nonNullable.control(''),
        email: this.fb.nonNullable.control(''),
        address: this.fb.nonNullable.control(''),
        staffType: this.fb.control<string | null>(null),
    });

    ngOnInit(): void {
        this.staffService.findById(this.staff()).subscribe({
            next: res => {
                const data = res.data;
                this.staffVersion = data.version;
                this.currentAvatarUrl = data.avatarUrl;
                this.previewUrl.set(this.resolveAvatarUrl(data.avatarUrl));
                this.updateForm.reset({
                    code: data.code,
                    fullName: data.fullName,
                    dob: data.dob ? new Date(data.dob) : null,
                    gender: data.gender,
                    phone: data.phone || '',
                    email: data.email || '',
                    address: data.address || '',
                    staffType: data.staffType,
                });
                this.schedules.set((data.workingSchedules || []).map(schedule => ({
                    uid: this.nextScheduleUid++,
                    dayOfWeek: schedule.dayOfWeek,
                    startTime: this.toTimeInput(schedule.startTime),
                    endTime: this.toTimeInput(schedule.endTime),
                })));
                this.fetching.set(false);
            },
            error: () => {
                this.fetching.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải thông tin nhân viên'});
                this.cancelled.emit();
            }
        });
    }

    ngOnDestroy(): void {
        this.revokePreviewUrl();
    }

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    addSchedule(): void {
        this.schedules.update(rows => [
            ...rows,
            {uid: this.nextScheduleUid++, dayOfWeek: '', startTime: '08:00', endTime: '17:00'},
        ]);
        this.clearError('workingSchedules');
    }

    removeSchedule(uid: number): void {
        this.schedules.update(rows => rows.filter(row => row.uid !== uid));
        this.clearError('workingSchedules');
    }

    updateSchedule(uid: number, field: keyof Omit<ScheduleRow, 'uid'>, value: string): void {
        this.schedules.update(rows => rows.map(row => row.uid === uid ? {...row, [field]: value} : row));
        this.clearError('workingSchedules');
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
            this.previewUrl.set(this.resolveAvatarUrl(this.currentAvatarUrl));
        }
    }

    private validate(): boolean {
        const val = this.updateForm.getRawValue();
        const errs: Record<string, string> = {};

        if (!val.fullName.trim()) errs['fullName'] = 'Vui lòng nhập họ và tên';
        if (!val.dob) errs['dob'] = 'Vui lòng chọn ngày sinh';
        if (val.gender === null) errs['gender'] = 'Vui lòng chọn giới tính';
        if (!val.phone.trim()) errs['phone'] = 'Vui lòng nhập số điện thoại';
        if (val.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val.email)) errs['email'] = 'Email không hợp lệ';
        if (!val.staffType) errs['staffType'] = 'Vui lòng chọn chức vụ';

        const schedules = this.buildWorkingSchedules();
        const hasPartialSchedule = this.schedules().some(row =>
            Boolean(row.dayOfWeek || row.startTime || row.endTime) &&
            (!row.dayOfWeek || !row.startTime || !row.endTime)
        );
        const duplicateDay = new Set(schedules.map(row => row.dayOfWeek)).size !== schedules.length;
        const invalidTime = schedules.some(row => row.startTime >= row.endTime);

        if (hasPartialSchedule) errs['workingSchedules'] = 'Vui lòng điền đủ thứ, giờ bắt đầu và giờ kết thúc';
        else if (duplicateDay) errs['workingSchedules'] = 'Mỗi ngày chỉ được tạo một lịch làm';
        else if (invalidTime) errs['workingSchedules'] = 'Giờ bắt đầu phải nhỏ hơn giờ kết thúc';

        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;

        const val = this.updateForm.getRawValue();
        const request: UpdateStaffRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: this.formatLocalDate(val.dob),
            gender: val.gender,
            phone: val.phone,
            email: val.email,
            address: val.address,
            staffType: val.staffType!,
            workingSchedules: this.buildWorkingSchedules(),
            version: this.staffVersion,
        };

        this.loading.set(true);
        this.staffService.update(this.staff(), request, this.selectedFile()).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật nhân viên thành công'});
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật nhân viên, vui lòng thử lại'});
            }
        });
    }

    private buildWorkingSchedules(): WorkingScheduleRequest[] {
        return this.schedules()
            .filter(row => row.dayOfWeek && row.startTime && row.endTime)
            .map(row => ({
                dayOfWeek: row.dayOfWeek,
                startTime: row.startTime,
                endTime: row.endTime,
            }));
    }

    private formatLocalDate(value: Date | null): string | null {
        if (!value) return null;
        const year = value.getFullYear();
        const month = `${value.getMonth() + 1}`.padStart(2, '0');
        const day = `${value.getDate()}`.padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    private toTimeInput(value: string | null | undefined): string {
        return value ? value.slice(0, 5) : '';
    }

    private resolveAvatarUrl(avatarUrl: string | null | undefined): string {
        if (!avatarUrl) return '/no-image-available-picture-coming-600nw-2057829641.jpg';
        if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) return avatarUrl;
        return `${ENV.API_BASE_URL}images/${avatarUrl.replace(/^\/+/, '')}`;
    }

    private revokePreviewUrl(): void {
        if (this.previewObjectUrl) {
            URL.revokeObjectURL(this.previewObjectUrl);
            this.previewObjectUrl = null;
        }
    }
}
