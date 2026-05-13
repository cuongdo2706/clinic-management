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
import {Tag} from 'primeng/tag';
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

type AppointmentAction = 'confirm' | 'checkIn' | 'start' | 'done' | 'cancel' | 'noShow';

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
        Tag,
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
        this.slotOptions.set([]);
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
        if (!this.formData.patientId || !appointmentDate) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Thiếu thông tin',
                detail: 'Vui lòng chọn bệnh nhân, ngày hẹn và giờ hẹn'
            });
            return;
        }

        this.saving.set(true);
        if (this.isEdit() && this.selectedId !== null) {
            const request: UpdateAppointmentRequest = {
                patientId: this.formData.patientId,
                dentistId: this.formData.dentistId,
                appointmentDate,
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
        this.slotOptions.set([]);
        if (!date || !dentistId) {
            return;
        }

        this.appointmentService.getAvailableSlots(dentistId, date).subscribe({
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
            error: () => this.slotOptions.set([]),
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

    confirmAction(appointment: AppointmentResponse, action: AppointmentAction) {
        this.confirmationService.confirm({
            message: this.actionConfirmMessage(appointment, action),
            header: this.actionConfirmHeader(action),
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: this.actionAcceptLabel(action),
            rejectLabel: 'Huỷ',
            acceptButtonStyleClass: this.actionAcceptButtonClass(action),
            accept: () => this.runAction(appointment, action),
        });
    }

    runAction(appointment: AppointmentResponse, action: AppointmentAction) {
        const request = switchAction(this.appointmentService, appointment.id, action);
        request.subscribe({
            next: () => {
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: this.actionSuccessMessage(action)});
                this.loadData();
            },
            error: (err: HttpErrorResponse) => this.showError(err),
        });
    }

    getStatusLabel(status: AppointmentStatus): string {
        return this.statusOptions.find(option => option.value === status)?.label ?? status;
    }

    getStatusSeverity(status: AppointmentStatus): 'success' | 'info' | 'warn' | 'danger' | 'secondary' | 'contrast' {
        switch (status) {
            case 'CONFIRMED':
            case 'DONE':
                return 'success';
            case 'PENDING':
                return 'warn';
            case 'IN_QUEUE':
                return 'info';
            case 'IN_PROGRESS':
                return 'contrast';
            case 'CANCELLED':
            case 'NO_SHOW':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    canConfirm(appointment: AppointmentResponse): boolean {
        return appointment.status === 'PENDING';
    }

    canCheckIn(appointment: AppointmentResponse): boolean {
        return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED';
    }

    canStart(appointment: AppointmentResponse): boolean {
        return appointment.status === 'IN_QUEUE' && Boolean(appointment.dentistId);
    }

    canDone(appointment: AppointmentResponse): boolean {
        return appointment.status === 'IN_PROGRESS';
    }

    canEdit(appointment: AppointmentResponse): boolean {
        return ['PENDING', 'CONFIRMED', 'IN_QUEUE'].includes(appointment.status);
    }

    canCancel(appointment: AppointmentResponse): boolean {
        return ['PENDING', 'CONFIRMED', 'IN_QUEUE'].includes(appointment.status);
    }

    canNoShow(appointment: AppointmentResponse): boolean {
        return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED';
    }

    canHardDelete(appointment: AppointmentResponse): boolean {
        return appointment.status === 'PENDING' || appointment.status === 'CONFIRMED';
    }

    getTimePlaceholder(): string {
        if (!this.appointmentDate) {
            return 'Chọn ngày';
        }
        if (!this.formData.dentistId) {
            return 'Chọn nha sĩ';
        }
        return 'Chọn giờ';
    }

    getTimeEmptyMessage(): string {
        if (!this.appointmentDate) {
            return 'Vui lòng chọn ngày hẹn';
        }
        if (!this.formData.dentistId) {
            return 'Vui lòng chọn nha sĩ';
        }
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

    private actionConfirmHeader(action: AppointmentAction): string {
        switch (action) {
            case 'done':
                return 'Hoàn tất lịch hẹn';
            case 'cancel':
                return 'Huỷ lịch hẹn';
            case 'noShow':
                return 'Đánh dấu không đến';
            default:
                return 'Xác nhận thao tác';
        }
    }

    private actionConfirmMessage(appointment: AppointmentResponse, action: AppointmentAction): string {
        switch (action) {
            case 'done':
                return `Hoàn tất lịch hẹn <b>${appointment.code}</b> của <b>${appointment.patientName}</b>?`;
            case 'cancel':
                return `Huỷ lịch hẹn <b>${appointment.code}</b> của <b>${appointment.patientName}</b>? Lịch hẹn sẽ được giữ lại trong lịch sử với trạng thái Đã hủy.`;
            case 'noShow':
                return `Đánh dấu bệnh nhân <b>${appointment.patientName}</b> không đến lịch hẹn <b>${appointment.code}</b>?`;
            default:
                return `Thực hiện thao tác với lịch hẹn <b>${appointment.code}</b>?`;
        }
    }

    private actionAcceptLabel(action: AppointmentAction): string {
        switch (action) {
            case 'done':
                return 'Hoàn tất';
            case 'cancel':
                return 'Huỷ lịch';
            case 'noShow':
                return 'Không đến';
            default:
                return 'Xác nhận';
        }
    }

    private actionAcceptButtonClass(action: AppointmentAction): string {
        switch (action) {
            case 'cancel':
                return 'p-button-danger';
            case 'noShow':
                return 'p-button-warn';
            default:
                return 'p-button-success';
        }
    }

    private actionSuccessMessage(action: AppointmentAction): string {
        switch (action) {
            case 'confirm':
                return 'Đã xác nhận lịch hẹn';
            case 'checkIn':
                return 'Đã check-in và cấp số thứ tự';
            case 'start':
                return 'Đã bắt đầu khám';
            case 'done':
                return 'Đã hoàn tất lịch hẹn';
            case 'cancel':
                return 'Đã hủy lịch hẹn';
            case 'noShow':
                return 'Đã đánh dấu không đến';
        }
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
}

function switchAction(
    service: AppointmentService,
    id: number,
    action: AppointmentAction
) {
    switch (action) {
        case 'confirm':
            return service.confirm(id);
        case 'checkIn':
            return service.checkIn(id);
        case 'start':
            return service.start(id);
        case 'done':
            return service.done(id);
        case 'cancel':
            return service.cancel(id);
        case 'noShow':
            return service.noShow(id);
    }
}
