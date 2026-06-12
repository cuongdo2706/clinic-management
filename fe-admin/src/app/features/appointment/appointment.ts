import {DatePipe} from '@angular/common';
import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {Observable} from 'rxjs';
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
import {AuthService} from '../../core/service/auth.service';
import {SuccessResponse} from '../../core/model/response/success-response';
import {
    AppointmentArrivalStatus,
    AppointmentResponse,
    AppointmentSortOption,
    AppointmentStatus,
    CreateAppointmentRequest,
    SearchAppointmentRequest,
    UpdateAppointmentRequest
} from './model/appointment.model';

interface SelectOption<T> {
    label: string;
    value: T;
}

type AppointmentAction = 'confirm' | 'arrived' | 'no-show' | 'cancel';

interface PendingStatusAction {
    appointment: AppointmentResponse;
    action: AppointmentAction;
    request: () => Observable<SuccessResponse<AppointmentResponse>>;
    successMessage: string;
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
    private readonly appointmentService = inject(AppointmentService);
    private readonly patientService = inject(PatientService);
    private readonly staffService = inject(StaffService);
    private readonly authService = inject(AuthService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    appointments = signal<AppointmentResponse[]>([]);
    dentistOptions = signal<SelectOption<number>[]>([]);
    slotOptions = signal<SelectOption<string>[]>([]);
    patientLookupItems = signal<PatientResponse[]>([]);
    patientLookupLoading = signal(false);
    patientLookupVisible = signal(false);
    patientLookupFirst = signal(0);
    patientLookupTotalRecords = signal(0);
    selectedPatientLabel = signal('');
    updateDateDialogVisible = signal(false);
    loading = signal(false);
    saving = signal(false);
    actionLoading = signal<string | null>(null);
    dialogVisible = signal(false);
    isEdit = signal(false);
    searchKeyword = signal('');

    statusFilter: AppointmentStatus | null = null;
    filterDate: Date | null = new Date();
    sortBy: AppointmentSortOption = 'APPOINTMENT_DATE';
    appointmentDate: Date | null = null;
    appointmentTime = '';
    patientLookupKeyword = '';
    patientLookupRows = 10;
    updateDateHour = 8;
    updateDateMinute = 30;
    pendingStatusAction: PendingStatusAction | null = null;
    selectedId: number | null = null;

    readonly statusOptions: SelectOption<AppointmentStatus>[] = [
        {label: 'Chờ xác nhận', value: 'PENDING'},
        {label: 'Đã xác nhận', value: 'CONFIRMED'},
        {label: 'Đang khám', value: 'IN_PROGRESS'},
        {label: 'Hoàn thành', value: 'COMPLETED'},
        {label: 'Đã hủy', value: 'CANCELLED'},
    ];

    readonly durationOptions: SelectOption<number>[] = [15, 30, 45, 60, 75, 90, 105, 120]
        .map(minutes => ({label: this.formatDurationLabel(minutes), value: minutes}));

    readonly sortOptions: SelectOption<AppointmentSortOption>[] = [
        {label: 'Gần nhất', value: 'APPOINTMENT_DATE'},
        {label: 'Xa nhất', value: 'APPOINTMENT_DATE_DESC'},
    ];

    readonly updateHourOptions: SelectOption<number>[] = Array.from({length: 13}, (_, index) => index + 8)
        .map(hour => ({label: String(hour).padStart(2, '0'), value: hour}));

    readonly updateMinuteOptions: SelectOption<number>[] = [0, 15, 30, 45]
        .map(minute => ({label: String(minute).padStart(2, '0'), value: minute}));

    formData: CreateAppointmentRequest & { version: number | null } = this.emptyForm();

    readonly isDentistView = this.authService.hasRole('DENTIST')
        && !this.authService.hasAnyRole(['ADMIN', 'MANAGER', 'RECEPTIONIST']);

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
        this.filterDate = new Date();
        this.sortBy = 'APPOINTMENT_DATE';
        this.loadData();
    }

    onFilterDateChange(value: Date | null) {
        this.filterDate = value;
        this.loadData();
    }

    openNew() {
        if (this.isDentistView) return;
        this.formData = this.emptyForm();
        this.appointmentDate = null;
        this.appointmentTime = '';
        this.selectedPatientLabel.set('');
        this.slotOptions.set([]);
        this.selectedId = null;
        this.isEdit.set(false);
        this.dialogVisible.set(true);
    }

    openEdit(appointment: AppointmentResponse) {
        if (this.isDentistView) return;
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
        this.selectedPatientLabel.set(this.toPatientLabel(
            appointment.patientCode,
            appointment.patientName,
            appointment.patientPhone
        ));
        this.isEdit.set(true);
        this.dialogVisible.set(true);
        this.loadAvailableSlots();
    }

    save() {
        if (this.isDentistView) return;
        const appointmentDate = this.toDateTimeString(this.appointmentDate, this.appointmentTime);
        if (!this.formData.patientId || !this.formData.dentistId || !appointmentDate || !this.formData.estimatedDurationMinutes) {
            this.messageService.add({
                severity: 'warn',
                summary: 'Thiếu thông tin',
                detail: 'Vui lòng chọn bệnh nhân, nha sĩ, ngày hẹn, giờ hẹn và thời gian dự kiến'
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
        this.slotOptions.set([]);
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
            error: () => this.slotOptions.set([]),
        });
    }

    confirmDelete(appointment: AppointmentResponse) {
        if (this.isDentistView) return;
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

    canEdit(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return ['PENDING', 'CONFIRMED'].includes(appointment.status)
            && appointment.arrivalStatus === 'NOT_ARRIVED';
    }

    canHardDelete(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return !['IN_PROGRESS', 'COMPLETED'].includes(appointment.status);
    }

    canConfirm(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return appointment.status === 'PENDING'
            && appointment.arrivalStatus === 'NOT_ARRIVED';
    }

    canMarkArrived(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return appointment.status === 'CONFIRMED'
            && appointment.arrivalStatus === 'NOT_ARRIVED';
    }

    canMarkNoShow(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return appointment.status === 'CONFIRMED'
            && appointment.arrivalStatus === 'NOT_ARRIVED';
    }

    canCancel(appointment: AppointmentResponse): boolean {
        if (this.isDentistView) return false;
        return ['PENDING', 'CONFIRMED'].includes(appointment.status)
            && appointment.arrivalStatus === 'NOT_ARRIVED';
    }

    confirmAppointment(appointment: AppointmentResponse): void {
        this.runAppointmentAction(
            appointment,
            'confirm',
            () => this.appointmentService.confirm(appointment.id),
            'Đã xác nhận lịch hẹn',
        );
    }

    markArrived(appointment: AppointmentResponse): void {
        this.runAppointmentAction(
            appointment,
            'arrived',
            () => this.appointmentService.checkIn(appointment.id),
            'Đã ghi nhận bệnh nhân đến',
        );
    }

    markNoShow(appointment: AppointmentResponse): void {
        this.runAppointmentAction(
            appointment,
            'no-show',
            () => this.appointmentService.noShow(appointment.id),
            'Đã đánh dấu bệnh nhân không đến',
        );
    }

    cancelAppointment(appointment: AppointmentResponse): void {
        this.confirmationService.confirm({
            message: `Hủy lịch hẹn <b>${appointment.code}</b> của <b>${appointment.patientName}</b>?`,
            header: 'Hủy lịch hẹn',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Hủy lịch',
            rejectLabel: 'Đóng',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => this.runAppointmentAction(
                appointment,
                'cancel',
                () => this.appointmentService.cancel(appointment.id),
                'Đã hủy lịch hẹn',
            ),
        });
    }

    isActionLoading(appointment: AppointmentResponse, action: AppointmentAction): boolean {
        return this.actionLoading() === this.actionKey(appointment, action);
    }

    getTimePlaceholder(): string {
        return 'Chọn giờ';
    }

    getTimeEmptyMessage(): string {
        return this.canSelectTime()
            ? 'Không có giờ trống'
            : 'Chọn nha sĩ, ngày hẹn và thời gian dự kiến trước';
    }

    canSelectTime(): boolean {
        return !!this.formData.dentistId && !!this.appointmentDate && !!this.formData.estimatedDurationMinutes;
    }

    openPatientLookup(): void {
        if (this.isDentistView) return;
        this.patientLookupVisible.set(true);
        this.patientLookupFirst.set(0);
        this.loadPatientLookup(0, this.patientLookupRows);
    }

    onPatientLookupSearch(): void {
        this.patientLookupFirst.set(0);
        this.loadPatientLookup(0, this.patientLookupRows);
    }

    onPatientLookupLazyLoad(event: { first?: number | null; rows?: number | null }): void {
        const rows = event.rows ?? this.patientLookupRows;
        const first = event.first ?? 0;
        this.patientLookupRows = rows;
        this.patientLookupFirst.set(first);
        this.loadPatientLookup(Math.floor(first / rows), rows);
    }

    selectPatient(patient: PatientResponse): void {
        this.formData.patientId = patient.id;
        this.selectedPatientLabel.set(this.toPatientOption(patient).label);
        this.patientLookupVisible.set(false);
    }

    private loadLookups() {
        this.loadPatientLookup(0, this.patientLookupRows);

        this.staffService.findDentistOptions().subscribe({
            next: (res) => this.dentistOptions.set((res.data ?? []).map(staff => this.toDentistOption(staff))),
        });
    }

    private loadPatientLookup(page: number, size: number): void {
        this.patientLookupLoading.set(true);
        this.patientService.search({
            page,
            size,
            sortBy: 'CREATED_AT_DESC',
            keyword: this.patientLookupKeyword,
            codeKeyword: '',
            nameKeyword: '',
            phoneKeyword: '',
            guardianNameKeyword: '',
            guardianPhoneKeyword: '',
        }).subscribe({
            next: (res) => {
                const data = res.data;
                this.patientLookupItems.set(data?.content ?? []);
                this.patientLookupTotalRecords.set(data?.totalElements ?? 0);
                this.patientLookupLoading.set(false);
            },
            error: () => {
                this.patientLookupItems.set([]);
                this.patientLookupTotalRecords.set(0);
                this.patientLookupLoading.set(false);
            },
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
            dateFrom: this.toDateString(this.filterDate),
            dateTo: this.toDateString(this.filterDate),
            sortBy: this.sortBy,
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
        this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: this.toUserSafeErrorMessage(err),
        });
    }

    private toUserSafeErrorMessage(err: HttpErrorResponse): string {
        const message = typeof err.error?.message === 'string' ? err.error.message : '';
        if (!message) {
            return 'Có lỗi xảy ra, vui lòng thử lại';
        }

        const lowerMessage = message.toLowerCase();
        const isInternalError = [
            'sql ',
            'constraint',
            'could not execute',
            'batch entry',
            'jdbc',
            'hibernate',
            'psqlexception',
            'violates check constraint',
        ].some(keyword => lowerMessage.includes(keyword));

        if (isInternalError) {
            return 'Không thể cập nhật dữ liệu, vui lòng kiểm tra lại hoặc thử lại sau';
        }

        return message;
    }

    statusLabel(status: AppointmentStatus): string {
        return this.statusOptions.find(option => option.value === status)?.label ?? status;
    }

    statusClass(status: AppointmentStatus | null | undefined): string {
        return status ? `status-badge status-${status.toLowerCase()}` : 'status-badge';
    }

    arrivalLabel(status: AppointmentArrivalStatus | null | undefined): string {
        switch (status) {
            case 'ARRIVED':
                return 'Đã đến';
            case 'NO_SHOW':
                return 'Không đến';
            case 'NOT_ARRIVED':
                return 'Chưa đến';
            default:
                return 'Chưa rõ';
        }
    }

    arrivalClass(status: AppointmentArrivalStatus | null | undefined): string {
        return status ? `arrival-badge arrival-${status.toLowerCase()}` : 'arrival-badge';
    }

    private runAppointmentAction(
        appointment: AppointmentResponse,
        action: AppointmentAction,
        request: () => Observable<SuccessResponse<AppointmentResponse>>,
        successMessage: string,
    ): void {
        if (!this.isAppointmentToday(appointment)) {
            this.openUpdateDateDialog({appointment, action, request, successMessage});
            return;
        }
        this.executeAppointmentAction(appointment, action, request, successMessage);
    }

    updateAppointmentToToday(): void {
        if (!this.pendingStatusAction) {
            this.updateDateDialogVisible.set(false);
            return;
        }
        const appointment = this.pendingStatusAction.appointment;
        const appointmentDate = this.todayDateTimeString(this.updateDateHour, this.updateDateMinute);
        const request: UpdateAppointmentRequest = {
            patientId: appointment.patientId,
            dentistId: appointment.dentistId,
            appointmentDate,
            estimatedDurationMinutes: appointment.estimatedDurationMinutes,
            symptom: appointment.symptom ?? '',
            note: appointment.note ?? '',
            version: appointment.version,
        };

        this.actionLoading.set(this.actionKey(appointment, this.pendingStatusAction.action));
        this.appointmentService.update(appointment.id, request).subscribe({
            next: () => {
                const pendingAction = this.pendingStatusAction;
                this.updateDateDialogVisible.set(false);
                this.pendingStatusAction = null;
                if (pendingAction) {
                    this.executeAppointmentAction(
                        pendingAction.appointment,
                        pendingAction.action,
                        pendingAction.request,
                        pendingAction.successMessage,
                    );
                }
            },
            error: (err: HttpErrorResponse) => {
                this.actionLoading.set(null);
                this.showError(err);
            },
        });
    }

    closeUpdateDateDialog(): void {
        this.pendingStatusAction = null;
        this.updateDateDialogVisible.set(false);
    }

    updateDatePromptText(): string {
        const appointmentDate = this.pendingStatusAction?.appointment.appointmentDate;
        const date = appointmentDate ? this.parseDateTime(appointmentDate) : null;
        const appointmentText = date ? `${this.toTimeString(date)} ${this.toDisplayDate(date)}` : 'ngày khác';
        const todayText = this.toDisplayDate(new Date());
        return `Ngày giờ của lịch hẹn này là ${appointmentText}. Bạn có muốn cập nhật lịch hẹn này sang ngày hôm nay ${todayText} không?`;
    }

    private executeAppointmentAction(
        appointment: AppointmentResponse,
        action: AppointmentAction,
        request: () => Observable<SuccessResponse<AppointmentResponse>>,
        successMessage: string,
    ): void {
        this.actionLoading.set(this.actionKey(appointment, action));
        request().subscribe({
            next: res => {
                this.actionLoading.set(null);
                if (res.data) {
                    this.appointments.update(items => items.map(item => item.id === res.data.id ? res.data : item));
                } else {
                    this.loadData();
                }
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: successMessage});
            },
            error: (err: HttpErrorResponse) => {
                this.actionLoading.set(null);
                this.showError(err);
            },
        });
    }

    private openUpdateDateDialog(pendingAction: PendingStatusAction): void {
        const date = this.parseDateTime(pendingAction.appointment.appointmentDate);
        this.updateDateHour = date ? date.getHours() : 8;
        this.updateDateMinute = date ? date.getMinutes() : 30;
        this.pendingStatusAction = pendingAction;
        this.updateDateDialogVisible.set(true);
    }

    private isAppointmentToday(appointment: AppointmentResponse): boolean {
        const date = this.parseDateTime(appointment.appointmentDate);
        if (!date) return false;
        return this.toDateString(date) === this.toDateString(new Date());
    }

    private todayDateTimeString(hour: number, minute: number): string {
        const today = this.toDateString(new Date());
        return `${today}T${String(hour).padStart(2, '0')}:${String(minute).padStart(2, '0')}:00`;
    }

    private toDisplayDate(date: Date): string {
        return `${String(date.getDate()).padStart(2, '0')}/${String(date.getMonth() + 1).padStart(2, '0')}/${date.getFullYear()}`;
    }

    private actionKey(appointment: AppointmentResponse, action: AppointmentAction): string {
        return `${appointment.id}:${action}`;
    }

    private toPatientOption(patient: PatientResponse): SelectOption<number> {
        const phone = patient.phone || patient.guardianPhone || 'Chưa có SĐT';
        return {label: this.toPatientLabel(patient.code, patient.fullName, phone), value: patient.id};
    }

    private toPatientLabel(code: string, fullName: string, phone: string | null): string {
        return `${code} - ${fullName} - ${phone || 'Chưa có SĐT'}`;
    }

    private toDentistOption(staff: Pick<StaffResponse, 'id' | 'code' | 'fullName'>): SelectOption<number> {
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
}
