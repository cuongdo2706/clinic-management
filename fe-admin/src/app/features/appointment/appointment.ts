import {DatePipe} from '@angular/common';
import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {MessageService, ConfirmationService} from 'primeng/api';
import {Button} from 'primeng/button';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {DatePicker} from 'primeng/datepicker';
import {Dialog} from 'primeng/dialog';
import {FloatLabel} from 'primeng/floatlabel';
import {IconField} from 'primeng/iconfield';
import {InputIcon} from 'primeng/inputicon';
import {InputText} from 'primeng/inputtext';
import {Select} from 'primeng/select';
import {TableModule} from 'primeng/table';
import {Textarea} from 'primeng/textarea';
import {Toast} from 'primeng/toast';
import {Toolbar} from 'primeng/toolbar';
import {Tooltip} from 'primeng/tooltip';
import {PatientResponse} from '../../core/model/response/patient-response';
import {StaffResponse} from '../../core/model/response/staff-response';
import {AppointmentService} from '../../core/service/appointment.service';
import {PatientService} from '../../core/service/patient.service';
import {StaffService} from '../../core/service/staff.service';
import {
    AppointmentResponse,
    AppointmentStatus,
    CreateAppointmentRequest,
    SearchAppointmentRequest,
    UpdateAppointmentRequest
} from './model/appointment.model';

interface SelectOption<T> {
    label: string;
    value: T;
}

@Component({
    selector: 'app-appointment',
    imports: [
        Toast,
        ConfirmDialog,
        TableModule,
        Button,
        Dialog,
        InputText,
        FloatLabel,
        FormsModule,
        IconField,
        InputIcon,
        Toolbar,
        DatePicker,
        Textarea,
        Select,
        Tooltip,
        DatePipe,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './appointment.html',
    styleUrl: './appointment.css',
})
export class Appointment implements OnInit {
    private static readonly DEFAULT_TIME_OPTIONS = Appointment.generateTimeOptions();

    private readonly appointmentService = inject(AppointmentService);
    private readonly patientService = inject(PatientService);
    private readonly staffService = inject(StaffService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    appointments = signal<AppointmentResponse[]>([]);
    patientOptions = signal<SelectOption<number>[]>([]);
    dentistOptions = signal<SelectOption<number>[]>([]);
    slotOptions = signal<SelectOption<string>[]>([]);
    loading = signal(false);
    saving = signal(false);
    updatingStatusId = signal<number | null>(null);
    dialogVisible = signal(false);
    isEdit = signal(false);
    searchKeyword = signal('');

    statusFilter: AppointmentStatus | null = null;
    appointmentDate: Date | null = null;
    appointmentTime = '';
    selectedId: number | null = null;

    readonly statusOptions: SelectOption<AppointmentStatus>[] = [
        {label: 'Chờ xác nhận', value: 'PENDING'},
        {label: 'Đã xác nhận', value: 'CONFIRMED'},
        {label: 'Đang chờ khám', value: 'IN_QUEUE'},
        {label: 'Đang khám', value: 'IN_PROGRESS'},
        {label: 'Hoàn thành', value: 'DONE'},
        {label: 'Đã hủy', value: 'CANCELLED'},
        {label: 'Không đến', value: 'NO_SHOW'},
    ];
    private readonly statusTransitions: Record<AppointmentStatus, AppointmentStatus[]> = {
        PENDING: ['CONFIRMED', 'IN_QUEUE', 'CANCELLED', 'NO_SHOW'],
        CONFIRMED: ['IN_QUEUE', 'CANCELLED', 'NO_SHOW'],
        IN_QUEUE: ['IN_PROGRESS', 'CANCELLED'],
        IN_PROGRESS: ['DONE'],
        DONE: [],
        CANCELLED: [],
        NO_SHOW: [],
    };

    readonly durationOptions: SelectOption<number>[] = [15, 30, 45, 60, 75, 90, 105, 120]
        .map(minutes => ({label: this.formatDurationLabel(minutes), value: minutes}));

    formData: CreateAppointmentRequest & { version: number | null } = this.emptyForm();

    ngOnInit() {
        this.loadLookups();
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.appointmentService.search(this.buildSearchRequest()).subscribe({
            next: (res) => {
                this.appointments.set(res.data?.content ?? []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false),
        });
    }

    onSearch() {
        this.loadData();
    }

    onResetFilter() {
        this.searchKeyword.set('');
        this.statusFilter = null;
        this.loadData();
    }

    openNew() {
        this.formData = this.emptyForm();
        this.appointmentDate = null;
        this.appointmentTime = '';
        this.slotOptions.set(Appointment.DEFAULT_TIME_OPTIONS);
        this.selectedId = null;
        this.isEdit.set(false);
        this.dialogVisible.set(true);
    }

    openEdit(appointment: AppointmentResponse) {
        this.selectedId = appointment.id;
        this.formData = {
            patientId: appointment.patientId,
            dentistId: appointment.dentistId,
            appointmentDate: appointment.appointmentDate,
            estimatedDurationMinutes: appointment.estimatedDurationMinutes ?? 30,
            symptom: appointment.symptom ?? '',
            note: appointment.note ?? '',
            version: appointment.version,
        };
        const date = this.parseDateTime(appointment.appointmentDate);
        this.appointmentDate = date;
        this.appointmentTime = date ? this.toTimeString(date) : '';
        this.isEdit.set(true);
        this.dialogVisible.set(true);
        this.loadAvailableSlots();
    }

    save() {
        const appointmentDate = this.toDateTimeString(this.appointmentDate, this.appointmentTime);
        if (!this.formData.patientId || !appointmentDate || !this.formData.estimatedDurationMinutes) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Thiếu thông tin',
                detail: 'Vui lòng chọn bệnh nhân, ngày hẹn, giờ hẹn và thời gian dự kiến'
            });
            return;
        }

        this.saving.set(true);
        if (this.isEdit() && this.selectedId !== null) {
            const request: UpdateAppointmentRequest = {
                patientId: this.formData.patientId,
                dentistId: this.formData.dentistId,
                appointmentDate,
                estimatedDurationMinutes: this.formData.estimatedDurationMinutes,
                symptom: this.formData.symptom,
                note: this.formData.note,
                version: this.formData.version,
            };
            this.appointmentService.update(this.selectedId, request).subscribe({
                next: () => this.afterSaved('Cập nhật lịch hẹn thành công'),
                error: (err: HttpErrorResponse) => this.afterSaveError(err),
            });
            return;
        }

        const request: CreateAppointmentRequest = {
            patientId: this.formData.patientId,
            dentistId: this.formData.dentistId,
            appointmentDate,
            estimatedDurationMinutes: this.formData.estimatedDurationMinutes,
            symptom: this.formData.symptom,
            note: this.formData.note,
        };
        this.appointmentService.create(request).subscribe({
            next: () => this.afterSaved('Thêm lịch hẹn thành công'),
            error: (err: HttpErrorResponse) => this.afterSaveError(err),
        });
    }

    onSlotContextChange() {
        this.appointmentTime = '';
        this.loadAvailableSlots();
    }

    loadAvailableSlots() {
        const date = this.toDateString(this.appointmentDate);
        const dentistId = this.formData.dentistId;
        const estimatedDurationMinutes = this.formData.estimatedDurationMinutes;
        this.slotOptions.set(Appointment.DEFAULT_TIME_OPTIONS);
        if (!date || !dentistId || !estimatedDurationMinutes) {
            return;
        }

        this.appointmentService.getAvailableSlots(dentistId, date, estimatedDurationMinutes).subscribe({
            next: (res) => {
                const slots = [...(res.data?.slots ?? [])];
                if (this.isEdit() && this.appointmentTime && !slots.includes(this.appointmentTime)) {
                    slots.unshift(this.appointmentTime);
                }
                this.slotOptions.set(slots.map(slot => ({label: slot, value: slot})));
                if (this.appointmentTime && !slots.includes(this.appointmentTime)) {
                    this.appointmentTime = '';
                }
            },
            error: () => this.slotOptions.set(Appointment.DEFAULT_TIME_OPTIONS),
        });
    }

    confirmDelete(appointment: AppointmentResponse) {
        this.confirmationService.confirm({
            message: `Xoá dữ liệu lịch hẹn <b>${appointment.code}</b> của <b>${appointment.patientName}</b>? Thao tác này chỉ dùng khi tạo nhầm dữ liệu, không dùng để huỷ lịch hẹn.`,
            header: 'Xoá dữ liệu lịch hẹn',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xoá dữ liệu',
            rejectLabel: 'Huỷ',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.appointmentService.delete(appointment.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xoá lịch hẹn'});
                        this.loadData();
                    },
                    error: (err: HttpErrorResponse) => this.showError(err),
                });
            },
        });
    }

    getAvailableStatusOptions(appointment: AppointmentResponse): SelectOption<AppointmentStatus>[] {
        const available = new Set([appointment.status, ...this.statusTransitions[appointment.status]]);
        return this.statusOptions.filter(option => available.has(option.value));
    }

    onStatusChange(appointment: AppointmentResponse, nextStatus: AppointmentStatus) {
        if (appointment.status === nextStatus) {
            return;
        }
        const update = () => this.updateStatus(appointment, nextStatus);
        if (nextStatus === 'CANCELLED' || nextStatus === 'NO_SHOW') {
            this.confirmationService.confirm({
                message: `Chuyển lịch hẹn <b>${appointment.code}</b> của <b>${appointment.patientName}</b> sang trạng thái <b>${this.statusLabel(nextStatus)}</b>?`,
                header: 'Xác nhận trạng thái',
                icon: 'pi pi-exclamation-triangle',
                acceptLabel: 'Xác nhận',
                rejectLabel: 'Huỷ',
                acceptButtonStyleClass: nextStatus === 'CANCELLED' ? 'p-button-danger' : 'p-button-warn',
                accept: update,
            });
            return;
        }
        update();
    }

    canEdit(appointment: AppointmentResponse): boolean {
        return ['PENDING', 'CONFIRMED', 'IN_QUEUE'].includes(appointment.status);
    }

    canHardDelete(appointment: AppointmentResponse): boolean {
        return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED';
    }

    getTimePlaceholder(): string {
        return 'Chọn giờ';
    }

    getTimeEmptyMessage(): string {
        return 'Không có giờ trống';
    }

    private loadLookups() {
        this.patientService.search({
            page: 0,
            size: 100,
            sortBy: 'CREATED_AT_DESC',
            codeKeyword: '',
            nameKeyword: '',
            phoneKeyword: '',
            guardianNameKeyword: '',
            guardianPhoneKeyword: '',
        }).subscribe({
            next: (res) => this.patientOptions.set((res.data?.content ?? []).map(patient => this.toPatientOption(patient))),
        });

        this.staffService.search({
            page: 0,
            size: 100,
            sortBy: 'NAME',
            codeKeyword: '',
            nameKeyword: '',
            phoneKeyword: '',
            staffType: 'DENTIST',
            isActive: true,
        }).subscribe({
            next: (res) => this.dentistOptions.set((res.data?.content ?? []).map(staff => this.toDentistOption(staff))),
        });
    }

    private buildSearchRequest(): SearchAppointmentRequest {
        return {
            page: 0,
            size: 100,
            keyword: this.searchKeyword(),
            codeKeyword: '',
            patientKeyword: '',
            dentistKeyword: '',
            status: this.statusFilter,
            dateFrom: null,
            dateTo: null,
            sortBy: 'APPOINTMENT_DATE_DESC',
        };
    }

    private emptyForm(): CreateAppointmentRequest & { version: number | null } {
        return {
            patientId: null,
            dentistId: null,
            appointmentDate: '',
            estimatedDurationMinutes: 30,
            symptom: '',
            note: '',
            version: null,
        };
    }

    private afterSaved(detail: string) {
        this.saving.set(false);
        this.messageService.add({severity: 'success', summary: 'Thành công', detail});
        this.dialogVisible.set(false);
        this.loadData();
    }

    private afterSaveError(err: HttpErrorResponse) {
        this.saving.set(false);
        this.showError(err);
    }

    private showError(err: HttpErrorResponse) {
        this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
    }

    private statusLabel(status: AppointmentStatus): string {
        return this.statusOptions.find(option => option.value === status)?.label ?? status;
    }

    private updateStatus(appointment: AppointmentResponse, status: AppointmentStatus) {
        this.updatingStatusId.set(appointment.id);
        this.appointmentService.updateStatus(appointment.id, status).subscribe({
            next: (res) => {
                const updated = res.data;
                if (updated) {
                    this.appointments.update(items => items.map(item => item.id === updated.id ? updated : item));
                }
                this.updatingStatusId.set(null);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật trạng thái lịch hẹn'});
            },
            error: (err: HttpErrorResponse) => {
                this.updatingStatusId.set(null);
                this.showError(err);
            },
        });
    }

    private toPatientOption(patient: PatientResponse): SelectOption<number> {
        const phone = patient.phone || patient.guardianPhone || 'Chưa có SĐT';
        return {label: `${patient.code} - ${patient.fullName} - ${phone}`, value: patient.id};
    }

    private toDentistOption(staff: StaffResponse): SelectOption<number> {
        return {label: `${staff.code} - ${staff.fullName}`, value: staff.id};
    }

    private toDateString(date: Date | null): string | null {
        if (!date || isNaN(date.getTime())) return null;
        const y = date.getFullYear();
        const m = String(date.getMonth() + 1).padStart(2, '0');
        const d = String(date.getDate()).padStart(2, '0');
        return `${y}-${m}-${d}`;
    }

    private toDateTimeString(date: Date | null, time: string): string {
        const dateText = this.toDateString(date);
        if (!dateText || !time) return '';
        return `${dateText}T${time}:00`;
    }

    private parseDateTime(value: string): Date | null {
        if (!value) return null;
        const date = new Date(value);
        return isNaN(date.getTime()) ? null : date;
    }

    private toTimeString(date: Date): string {
        return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`;
    }

    private formatDurationLabel(minutes: number): string {
        if (minutes <= 60) {
            return `${minutes} phút`;
        }
        const hours = Math.floor(minutes / 60);
        const remainingMinutes = minutes % 60;
        return remainingMinutes === 0
            ? `${hours} giờ`
            : `${hours} giờ ${remainingMinutes} phút`;
    }

    private static generateTimeOptions(): SelectOption<string>[] {
        const options: SelectOption<string>[] = [];
        for (let hour = 8; hour < 17; hour++) {
            for (const minute of [0, 15, 30, 45]) {
                const value = `${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}`;
                options.push({label: value, value});
            }
        }
        return options;
    }
}
