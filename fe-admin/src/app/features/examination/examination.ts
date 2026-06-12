import {DatePipe} from '@angular/common';
import {HttpErrorResponse} from '@angular/common/http';
import {Component, inject, OnInit, signal} from '@angular/core';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Observable} from 'rxjs';
import {ConfirmationService, MessageService} from 'primeng/api';
import {Button} from 'primeng/button';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {FloatLabel} from 'primeng/floatlabel';
import {InputText} from 'primeng/inputtext';
import {ProgressSpinner} from 'primeng/progressspinner';
import {Select} from 'primeng/select';
import {TableModule} from 'primeng/table';
import {Tab, TabList, TabPanel, TabPanels, Tabs} from 'primeng/tabs';
import {Tag} from 'primeng/tag';
import {Textarea} from 'primeng/textarea';
import {Toast} from 'primeng/toast';
import {Tooltip} from 'primeng/tooltip';
import {ExaminationService} from '../../core/service/examination.service';
import {
    CreateTreatmentRequest,
    PrescriptionItemRequest,
    PrescriptionRequest,
    TreatmentProcedureRequest,
    UpdateTreatmentRequest,
} from '../../core/model/request/treatment-request';
import {
    PatientDetailResponse,
    PrescriptionDetailResponse,
    TreatmentDetailResponse,
    TreatmentStatus,
    TreatmentSummaryResponse,
} from '../../core/model/response/patient-detail-response';
import {MedicineResponse} from '../../core/model/response/medicine-response';
import {ProcedureResponse} from '../../core/model/response/procedure-response';
import {StaffResponse} from '../../core/model/response/staff-response';
import {SuccessResponse} from '../../core/model/response/success-response';
import {
    AppointmentArrivalStatus,
    AppointmentResponse,
    AppointmentStatus,
} from '../appointment/model/appointment.model';

type ExamAction = 'start' | 'done';
type ExamStatusFilter = 'ALL' | 'WAITING' | 'IN_PROGRESS';

interface ProcedureDraftRow {
    key: number;
    procedureId: number | null;
    code: string | null;
    name: string | null;
    unit: string | null;
    quantity: number;
    unitPrice: number | null;
    note: string;
}

interface MedicineDraftRow {
    key: number;
    medicineId: number | null;
    code: string | null;
    name: string | null;
    unit: string | null;
    quantity: number;
    dosage: string;
    frequency: string;
    duration: string;
    instruction: string;
}

@Component({
    selector: 'app-examination',
    imports: [
        Button,
        DatePipe,
        FloatLabel,
        FormsModule,
        InputText,
        ProgressSpinner,
        ReactiveFormsModule,
        Select,
        TableModule,
        Tabs,
        TabList,
        Tab,
        TabPanels,
        TabPanel,
        Tag,
        Textarea,
        Toast,
        ConfirmDialog,
        Tooltip,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './examination.html',
    styleUrl: './examination.css',
})
export class Examination implements OnInit {
    private readonly examinationService = inject(ExaminationService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly fb = inject(FormBuilder);
    private draftRowKey = 0;

    dentist = signal<StaffResponse | null>(null);
    appointments = signal<AppointmentResponse[]>([]);
    expandedRows = signal<{ [key: string]: boolean }>({});
    patientDetails = signal<Record<number, PatientDetailResponse>>({});
    detailLoading = signal<Record<number, boolean>>({});
    treatmentDetails = signal<Record<number, TreatmentDetailResponse>>({});
    treatmentLoading = signal<Record<number, boolean>>({});
    procedureOptions = signal<ProcedureResponse[]>([]);
    medicineOptions = signal<MedicineResponse[]>([]);
    procedureDrafts = signal<ProcedureDraftRow[]>([this.createEmptyProcedureDraft()]);
    medicineDrafts = signal<MedicineDraftRow[]>([this.createEmptyMedicineDraft()]);
    loading = signal(false);
    resolvingDentist = signal(false);
    savingTreatment = signal(false);
    savingProcedure = signal(false);
    savingPrescription = signal(false);
    actionLoading = signal<string | null>(null);
    error = signal('');
    todayLabel = signal('');

    readonly statusOptions: { label: string; value: ExamStatusFilter }[] = [
        {label: 'Tất cả ca khám', value: 'ALL'},
        {label: 'Chờ khám', value: 'WAITING'},
        {label: 'Đang khám', value: 'IN_PROGRESS'},
    ];

    searchForm = this.fb.group({
        keyword: this.fb.nonNullable.control(''),
        examStatus: this.fb.nonNullable.control<ExamStatusFilter>('ALL'),
        dateFrom: this.fb.nonNullable.control(''),
        dateTo: this.fb.nonNullable.control(''),
    });

    procedureForm = this.fb.group({
        procedureId: this.fb.control<number | null>(null),
        quantity: this.fb.nonNullable.control(1),
        note: this.fb.nonNullable.control(''),
    });

    medicineForm = this.fb.group({
        medicineId: this.fb.control<number | null>(null),
        dosage: this.fb.nonNullable.control(''),
        frequency: this.fb.nonNullable.control(''),
        duration: this.fb.nonNullable.control(''),
        quantity: this.fb.nonNullable.control(1),
        instruction: this.fb.nonNullable.control(''),
        advice: this.fb.nonNullable.control(''),
        reExaminationDate: this.fb.nonNullable.control(''),
    });

    treatmentForm = this.fb.group({
        diagnosis: this.fb.nonNullable.control(''),
        note: this.fb.nonNullable.control(''),
    });

    ngOnInit(): void {
        const today = this.toDateString(new Date());
        this.searchForm.patchValue({dateFrom: today, dateTo: today});
        this.todayLabel.set(new Intl.DateTimeFormat('vi-VN', {
            weekday: 'long',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        }).format(new Date()));
        this.loadCatalogs();
        this.resolveCurrentDentist();
    }

    onSearch(): void {
        if (!this.dentist()) {
            this.resolveCurrentDentist();
            return;
        }
        this.loadAppointments();
    }

    onResetFilter(): void {
        const today = this.toDateString(new Date());
        this.searchForm.reset({
            keyword: '',
            examStatus: 'ALL',
            dateFrom: today,
            dateTo: today,
        });
        this.loadAppointments();
    }

    reload(): void {
        this.expandedRows.set({});
        this.onSearch();
    }

    toggleRow(appointment: AppointmentResponse): void {
        const id = appointment.id.toString();
        const current = this.expandedRows();

        if (current[id]) {
            this.expandedRows.set({});
            return;
        }

        this.expandedRows.set({[id]: true});
        this.resetTreatmentForm(null);
        this.resetProcedureDrafts(null);
        this.resetMedicineDrafts(null);
        this.loadPatientDetail(appointment);
    }

    startAppointment(appointment: AppointmentResponse): void {
        if (!this.isTodayAppointment(appointment)) {
            this.confirmStartDifferentDate(appointment);
            return;
        }
        this.runStartAppointment(appointment);
    }

    completeAppointment(appointment: AppointmentResponse): void {
        if (!this.currentTreatment(appointment)) {
            this.showWarn('Vui lòng lưu phiếu khám trước khi kết thúc khám');
            return;
        }
        this.runAction(appointment, 'done', () => this.examinationService.done(appointment.id), 'Đã hoàn tất buổi khám');
    }

    canStart(appointment: AppointmentResponse): boolean {
        return appointment.status === 'CONFIRMED'
            && appointment.arrivalStatus === 'ARRIVED';
    }

    canComplete(appointment: AppointmentResponse): boolean {
        return appointment.status === 'IN_PROGRESS' && !!this.currentTreatment(appointment);
    }

    isTodayAppointment(appointment: AppointmentResponse): boolean {
        const appointmentDate = new Date(appointment.appointmentDate);
        if (Number.isNaN(appointmentDate.getTime())) {
            return false;
        }
        const today = new Date();
        return appointmentDate.getFullYear() === today.getFullYear()
            && appointmentDate.getMonth() === today.getMonth()
            && appointmentDate.getDate() === today.getDate();
    }

    isActionLoading(appointment: AppointmentResponse, action: ExamAction): boolean {
        return this.actionLoading() === this.actionKey(appointment, action);
    }

    isDetailLoading(patientId: number): boolean {
        return !!this.detailLoading()[patientId];
    }

    isTreatmentLoading(treatmentId: number): boolean {
        return !!this.treatmentLoading()[treatmentId];
    }

    patientDetail(appointment: AppointmentResponse): PatientDetailResponse | null {
        return this.patientDetails()[appointment.patientId] ?? null;
    }

    currentTreatment(appointment: AppointmentResponse): TreatmentSummaryResponse | null {
        const detail = this.patientDetail(appointment);
        if (!detail) {
            return null;
        }
        return detail.treatments.find(treatment => treatment.appointmentId === appointment.id) ?? null;
    }

    currentTreatmentDetail(appointment: AppointmentResponse): TreatmentDetailResponse | null {
        const treatment = this.currentTreatment(appointment);
        if (!treatment) {
            return null;
        }
        return this.treatmentDetails()[treatment.id] ?? null;
    }

    currentPrescription(appointment: AppointmentResponse): PrescriptionDetailResponse | null {
        const treatmentDetail = this.currentTreatmentDetail(appointment);
        if (treatmentDetail?.prescription) {
            return treatmentDetail.prescription;
        }

        const treatment = this.currentTreatment(appointment);
        if (!treatment) {
            return null;
        }

        return this.patientDetail(appointment)?.prescriptions.find(prescription => prescription.treatmentId === treatment.id) ?? null;
    }

    isExamEditable(appointment: AppointmentResponse): boolean {
        return appointment.status === 'IN_PROGRESS';
    }

    canEditTreatmentPayload(appointment: AppointmentResponse): boolean {
        const treatment = this.currentTreatment(appointment);
        return this.isExamEditable(appointment) && (!treatment || !!this.currentTreatmentDetail(appointment));
    }

    saveTreatmentSummary(appointment: AppointmentResponse): void {
        if (!this.isExamEditable(appointment)) {
            this.showWarn('Chỉ lưu phiếu khám khi lịch đang ở trạng thái Đang khám');
            return;
        }

        const diagnosis = this.treatmentForm.controls.diagnosis.value.trim();
        const note = this.treatmentForm.controls.note.value.trim();
        if (!diagnosis) {
            this.showWarn('Vui lòng nhập kết luận hoặc chẩn đoán của buổi khám');
            return;
        }

        this.savingTreatment.set(true);
        this.persistTreatment(
            appointment,
            {
                diagnosis,
                note: note || null,
            },
            () => this.savingTreatment.set(false),
            'Đã lưu phiếu khám',
        );
    }

    selectedProcedure(procedureId: number | null | undefined): ProcedureResponse | null {
        return this.procedureOptions().find(item => item.id === procedureId) ?? null;
    }

    selectedMedicine(medicineId: number | null | undefined): MedicineResponse | null {
        return this.medicineOptions().find(item => item.id === medicineId) ?? null;
    }

    procedureTotal(): number {
        return this.procedureDrafts().reduce((total, row) => {
            if (!row.procedureId) {
                return total;
            }
            return total + (row.quantity || 0) * (row.unitPrice || 0);
        }, 0);
    }

    onProcedureDraftSelected(rowKey: number, procedureId: number | null): void {
        const selectedProcedure = this.selectedProcedure(procedureId);
        this.procedureDrafts.update(rows => {
            const rowIndex = rows.findIndex(row => row.key === rowKey);
            if (rowIndex < 0) {
                return this.ensureEmptyProcedureDraft(rows);
            }

            const duplicatedIndex = rows.findIndex((row, index) =>
                index !== rowIndex && row.procedureId !== null && row.procedureId === procedureId);
            if (procedureId !== null && duplicatedIndex >= 0) {
                const duplicatedRow = rows[duplicatedIndex];
                const currentRow = rows[rowIndex];
                const mergedRows = rows
                    .map((row, index) => index === duplicatedIndex
                        ? {
                            ...duplicatedRow,
                            quantity: (duplicatedRow.quantity || 0) + (currentRow.quantity || 1),
                            unitPrice: selectedProcedure?.price ?? duplicatedRow.unitPrice,
                            code: selectedProcedure?.code ?? duplicatedRow.code,
                            name: selectedProcedure?.name ?? duplicatedRow.name,
                            unit: selectedProcedure?.unit ?? duplicatedRow.unit,
                        }
                        : row)
                    .filter(row => row.key !== rowKey);
                return this.ensureEmptyProcedureDraft(mergedRows);
            }

            const nextRows = rows.map(row => row.key === rowKey
                ? {
                    ...row,
                    procedureId,
                    code: selectedProcedure?.code ?? null,
                    name: selectedProcedure?.name ?? null,
                    unit: selectedProcedure?.unit ?? null,
                    quantity: row.quantity || 1,
                    unitPrice: selectedProcedure?.price ?? row.unitPrice,
                }
                : row);
            return this.ensureEmptyProcedureDraft(nextRows);
        });
    }

    updateProcedureDraft(rowKey: number, patch: Partial<ProcedureDraftRow>): void {
        this.procedureDrafts.update(rows => this.ensureEmptyProcedureDraft(rows.map(row =>
            row.key === rowKey ? {...row, ...patch} : row)));
    }

    removeProcedureDraft(rowKey: number): void {
        this.procedureDrafts.update(rows => this.ensureEmptyProcedureDraft(rows.filter(row => row.key !== rowKey)));
    }

    saveProcedures(appointment: AppointmentResponse): void {
        if (!this.canEditTreatmentPayload(appointment)) {
            this.showWarn('Chỉ lưu dịch vụ khi lịch đang ở trạng thái Đang khám');
            return;
        }

        const procedures = this.compactProcedureDrafts();
        if (procedures.some(item => !Number.isFinite(item.quantity ?? 0) || (item.quantity ?? 0) < 1)) {
            this.showWarn('Số lượng dịch vụ phải lớn hơn 0');
            return;
        }

        this.savingProcedure.set(true);
        this.persistTreatment(
            appointment,
            {procedures},
            () => this.savingProcedure.set(false),
            'Đã lưu dịch vụ khám',
        );
    }

    onMedicineDraftSelected(rowKey: number, medicineId: number | null): void {
        const selectedMedicine = this.selectedMedicine(medicineId);
        this.medicineDrafts.update(rows => {
            const rowIndex = rows.findIndex(row => row.key === rowKey);
            if (rowIndex < 0) {
                return this.ensureEmptyMedicineDraft(rows);
            }

            const duplicatedIndex = rows.findIndex((row, index) =>
                index !== rowIndex && row.medicineId !== null && row.medicineId === medicineId);
            if (medicineId !== null && duplicatedIndex >= 0) {
                const duplicatedRow = rows[duplicatedIndex];
                const currentRow = rows[rowIndex];
                const mergedRows = rows
                    .map((row, index) => index === duplicatedIndex
                        ? {
                            ...duplicatedRow,
                            quantity: (duplicatedRow.quantity || 0) + (currentRow.quantity || 1),
                            dosage: currentRow.dosage || duplicatedRow.dosage,
                            frequency: currentRow.frequency || duplicatedRow.frequency,
                            duration: currentRow.duration || duplicatedRow.duration,
                            instruction: currentRow.instruction || duplicatedRow.instruction,
                            code: selectedMedicine?.code ?? duplicatedRow.code,
                            name: selectedMedicine?.name ?? duplicatedRow.name,
                            unit: selectedMedicine?.unit ?? duplicatedRow.unit,
                        }
                        : row)
                    .filter(row => row.key !== rowKey);
                return this.ensureEmptyMedicineDraft(mergedRows);
            }

            const nextRows = rows.map(row => row.key === rowKey
                ? {
                    ...row,
                    medicineId,
                    code: selectedMedicine?.code ?? null,
                    name: selectedMedicine?.name ?? null,
                    unit: selectedMedicine?.unit ?? null,
                    quantity: row.quantity || 1,
                }
                : row);
            return this.ensureEmptyMedicineDraft(nextRows);
        });
    }

    updateMedicineDraft(rowKey: number, patch: Partial<MedicineDraftRow>): void {
        this.medicineDrafts.update(rows => this.ensureEmptyMedicineDraft(rows.map(row =>
            row.key === rowKey ? {...row, ...patch} : row)));
    }

    removeMedicineDraft(rowKey: number): void {
        this.medicineDrafts.update(rows => this.ensureEmptyMedicineDraft(rows.filter(row => row.key !== rowKey)));
    }

    savePrescription(appointment: AppointmentResponse): void {
        if (!this.canEditTreatmentPayload(appointment)) {
            this.showWarn('Chỉ lưu đơn thuốc khi lịch đang ở trạng thái Đang khám');
            return;
        }

        const items = this.compactMedicineDrafts();
        if (items.some(item => !Number.isFinite(item.quantity ?? 0) || (item.quantity ?? 0) < 1)) {
            this.showWarn('Số lượng thuốc phải lớn hơn 0');
            return;
        }

        const currentPrescription = this.currentPrescription(appointment);
        const existingPrescriptionRequest = currentPrescription
            ? this.toPrescriptionRequest(currentPrescription)
            : this.createEmptyPrescriptionRequest(appointment);
        const prescriptionMeta = this.medicineForm.getRawValue();

        const prescription: PrescriptionRequest = {
            ...existingPrescriptionRequest,
            advice: this.emptyToNull(prescriptionMeta.advice),
            reExaminationDate: this.emptyToNull(prescriptionMeta.reExaminationDate),
            items,
        };

        this.savingPrescription.set(true);
        this.persistTreatment(
            appointment,
            {prescription},
            () => this.savingPrescription.set(false),
            'Đã lưu đơn thuốc',
        );
    }

    examStatusLabel(appointment: AppointmentResponse): string {
        return appointment.status === 'IN_PROGRESS' ? 'Đang khám' : 'Chờ khám';
    }

    examStatusSeverity(appointment: AppointmentResponse): 'info' | 'success' | 'warn' | 'danger' | 'secondary' {
        return appointment.status === 'IN_PROGRESS' ? 'warn' : 'info';
    }

    arrivalSeverity(status: AppointmentArrivalStatus | null | undefined): 'info' | 'success' | 'warn' | 'danger' | 'secondary' {
        switch (status) {
            case 'ARRIVED':
                return 'success';
            case 'NO_SHOW':
                return 'danger';
            case 'NOT_ARRIVED':
                return 'info';
            default:
                return 'secondary';
        }
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
                return 'Chưa đến';
        }
    }

    appointmentStatusLabel(status: AppointmentStatus | null | undefined): string {
        switch (status) {
            case 'PENDING':
                return 'Chờ xác nhận';
            case 'CONFIRMED':
                return 'Đã xác nhận';
            case 'IN_PROGRESS':
                return 'Đang khám';
            case 'COMPLETED':
                return 'Hoàn thành';
            case 'CANCELLED':
                return 'Đã hủy';
            default:
                return '—';
        }
    }

    appointmentStatusSeverity(status: AppointmentStatus | null | undefined): 'info' | 'success' | 'warn' | 'danger' | 'secondary' {
        switch (status) {
            case 'PENDING':
                return 'warn';
            case 'CONFIRMED':
                return 'info';
            case 'IN_PROGRESS':
                return 'warn';
            case 'COMPLETED':
                return 'success';
            case 'CANCELLED':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    treatmentStatusLabel(status: TreatmentStatus | null | undefined): string {
        switch (status) {
            case 'IN_PROGRESS':
                return 'Đang điều trị';
            case 'COMPLETED':
                return 'Hoàn thành';
            case 'CANCELLED':
                return 'Đã hủy';
            default:
                return '—';
        }
    }

    treatmentStatusSeverity(status: TreatmentStatus | null | undefined): 'info' | 'success' | 'warn' | 'danger' | 'secondary' {
        switch (status) {
            case 'IN_PROGRESS':
                return 'info';
            case 'COMPLETED':
                return 'success';
            case 'CANCELLED':
                return 'danger';
            default:
                return 'secondary';
        }
    }

    genderLabel(gender: boolean | null | undefined): string {
        if (gender === null || gender === undefined) {
            return '—';
        }
        return gender ? 'Nam' : 'Nữ';
    }

    activeLabel(active: boolean | null | undefined): string {
        return active === false ? 'Ngừng hoạt động' : 'Hoạt động';
    }

    formatTime(value: string): string {
        return new Intl.DateTimeFormat('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
        }).format(new Date(value));
    }

    formatDateTime(value: string): string {
        return new Intl.DateTimeFormat('vi-VN', {
            hour: '2-digit',
            minute: '2-digit',
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
        }).format(new Date(value));
    }

    private resolveCurrentDentist(): void {
        this.resolvingDentist.set(true);
        this.error.set('');

        this.examinationService.currentDentist().subscribe({
            next: (res) => {
                const matched = res.data ?? null;
                this.dentist.set(matched);
                this.resolvingDentist.set(false);

                if (!matched) {
                    this.error.set('Không tìm thấy hồ sơ nha sĩ tương ứng với tài khoản hiện tại.');
                    return;
                }

                this.loadAppointments();
            },
            error: () => {
                this.resolvingDentist.set(false);
                this.error.set('Không tải được thông tin nha sĩ hiện tại.');
            },
        });
    }

    private loadAppointments(): void {
        const currentDentist = this.dentist();
        if (!currentDentist) {
            return;
        }

        const formValue = this.searchForm.getRawValue();
        const apiStatus = formValue.examStatus === 'IN_PROGRESS' ? 'IN_PROGRESS' : null;

        this.loading.set(true);
        this.error.set('');
        this.examinationService.searchAppointments({
            page: 0,
            size: 200,
            keyword: formValue.keyword.trim(),
            codeKeyword: '',
            patientKeyword: '',
            dentistKeyword: '',
            status: apiStatus,
            dateFrom: formValue.dateFrom || null,
            dateTo: formValue.dateTo || null,
            sortBy: 'APPOINTMENT_DATE',
        }).subscribe({
            next: (res) => {
                const appointments = (res.data?.content ?? [])
                    .filter(item => item.dentistId === currentDentist.id)
                    .filter(item => this.isVisibleExamAppointment(item, formValue.examStatus))
                    .sort((a, b) => new Date(a.appointmentDate).getTime() - new Date(b.appointmentDate).getTime());
                this.appointments.set(appointments);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.error.set('Không tải được danh sách ca khám.');
            },
        });
    }

    private loadPatientDetail(appointment: AppointmentResponse): void {
        const patientId = appointment.patientId;
        if (this.patientDetails()[patientId]) {
            this.loadTreatmentDetailForAppointment(appointment);
            return;
        }
        if (this.detailLoading()[patientId]) {
            return;
        }

        this.detailLoading.update(state => ({...state, [patientId]: true}));
        this.examinationService.patientDetail(patientId).subscribe({
            next: (res) => {
                if (res.data) {
                    this.patientDetails.update(state => ({...state, [patientId]: res.data}));
                    this.loadTreatmentDetailForAppointment(appointment);
                }
                this.detailLoading.update(state => ({...state, [patientId]: false}));
            },
            error: () => {
                this.detailLoading.update(state => ({...state, [patientId]: false}));
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải chi tiết bệnh nhân',
                });
            },
        });
    }

    private loadTreatmentDetailForAppointment(appointment: AppointmentResponse): void {
        const treatment = this.currentTreatment(appointment);
        if (!treatment) {
            this.resetTreatmentForm(null);
            this.resetProcedureDrafts(null);
            this.resetMedicineDrafts(null);
            return;
        }
        if (this.treatmentDetails()[treatment.id]) {
            this.resetDraftsFromTreatment(this.treatmentDetails()[treatment.id]);
            return;
        }
        if (this.treatmentLoading()[treatment.id]) {
            return;
        }

        this.resetTreatmentForm(treatment);
        this.treatmentLoading.update(state => ({...state, [treatment.id]: true}));
        this.examinationService.findTreatmentById(treatment.id).subscribe({
            next: (res) => {
                if (res.data) {
                    this.treatmentDetails.update(state => ({...state, [treatment.id]: res.data}));
                    this.resetDraftsFromTreatment(res.data);
                }
                this.treatmentLoading.update(state => ({...state, [treatment.id]: false}));
            },
            error: () => {
                this.treatmentLoading.update(state => ({...state, [treatment.id]: false}));
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải chi tiết phiếu điều trị',
                });
            },
        });
    }

    private loadCatalogs(): void {
        this.examinationService.searchProcedures({
            page: 0,
            size: 200,
            sortBy: 'NAME',
            codeKeyword: '',
            nameKeyword: '',
            priceFrom: null,
            priceTo: null,
        }).subscribe({
            next: res => this.procedureOptions.set((res.data?.content ?? []).filter(item => item.isActive !== false)),
            error: () => this.messageService.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Không tải được danh sách dịch vụ khám',
            }),
        });

        this.examinationService.searchMedicines({
            page: 0,
            size: 200,
            sortBy: 'NAME',
            codeKeyword: '',
            nameKeyword: '',
            priceFrom: null,
            priceTo: null,
        }).subscribe({
            next: res => this.medicineOptions.set((res.data?.content ?? []).filter(item => item.isActive !== false)),
            error: () => this.messageService.add({
                severity: 'error',
                summary: 'Lỗi',
                detail: 'Không tải được danh sách thuốc',
            }),
        });
    }

    private persistTreatment(
        appointment: AppointmentResponse,
        overrides: Partial<Pick<CreateTreatmentRequest, 'diagnosis' | 'note' | 'status' | 'procedures' | 'prescription'>>,
        finalize: () => void,
        successMessage: string,
    ): void {
        const treatmentDetail = this.currentTreatmentDetail(appointment);
        const request = this.buildTreatmentRequest(appointment, treatmentDetail, overrides);
        const source = treatmentDetail
            ? this.examinationService.updateTreatment(treatmentDetail.id, this.toUpdateRequest(request, treatmentDetail.version))
            : this.examinationService.createTreatment(request);

        source.subscribe({
            next: res => {
                finalize();
                if (res.data) {
                    this.treatmentDetails.update(state => ({...state, [res.data.id]: res.data}));
                    this.resetDraftsFromTreatment(res.data);
                }
                this.refreshPatientDetail(appointment);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: successMessage,
                });
            },
            error: (err: HttpErrorResponse) => {
                finalize();
                this.showError(err, 'Không thể lưu phiếu khám');
            },
        });
    }

    private buildTreatmentRequest(
        appointment: AppointmentResponse,
        treatmentDetail: TreatmentDetailResponse | null,
        overrides: Partial<Pick<CreateTreatmentRequest, 'diagnosis' | 'note' | 'status' | 'procedures' | 'prescription'>>,
    ): CreateTreatmentRequest {
        return {
            patientId: appointment.patientId,
            appointmentId: appointment.id,
            doctorId: appointment.dentistId,
            diagnosis: overrides.diagnosis !== undefined
                ? overrides.diagnosis
                : treatmentDetail?.diagnosis ?? null,
            note: overrides.note !== undefined
                ? overrides.note
                : treatmentDetail?.note ?? null,
            treatmentDate: treatmentDetail?.treatmentDate ?? this.toLocalDateTime(new Date()),
            status: overrides.status !== undefined
                ? overrides.status
                : treatmentDetail?.status ?? 'IN_PROGRESS',
            prescription: overrides.prescription !== undefined
                ? overrides.prescription
                : treatmentDetail?.prescription ? this.toPrescriptionRequest(treatmentDetail.prescription) : null,
            procedures: overrides.procedures !== undefined
                ? overrides.procedures
                : this.toProcedureRequests(treatmentDetail),
        };
    }

    private toUpdateRequest(request: CreateTreatmentRequest, version: number | null): UpdateTreatmentRequest {
        return {
            appointmentId: request.appointmentId,
            doctorId: request.doctorId,
            diagnosis: request.diagnosis,
            note: request.note,
            treatmentDate: request.treatmentDate,
            status: request.status,
            prescription: request.prescription,
            procedures: request.procedures,
            version,
        };
    }

    private createEmptyProcedureDraft(): ProcedureDraftRow {
        return {
            key: ++this.draftRowKey,
            procedureId: null,
            code: null,
            name: null,
            unit: null,
            quantity: 1,
            unitPrice: null,
            note: '',
        };
    }

    private createEmptyMedicineDraft(): MedicineDraftRow {
        return {
            key: ++this.draftRowKey,
            medicineId: null,
            code: null,
            name: null,
            unit: null,
            quantity: 1,
            dosage: '',
            frequency: '',
            duration: '',
            instruction: '',
        };
    }

    private ensureEmptyProcedureDraft(rows: ProcedureDraftRow[]): ProcedureDraftRow[] {
        const selectedRows = rows.filter(row => row.procedureId !== null);
        const emptyRow = rows.find(row => row.procedureId === null) ?? this.createEmptyProcedureDraft();
        return [...selectedRows, emptyRow];
    }

    private ensureEmptyMedicineDraft(rows: MedicineDraftRow[]): MedicineDraftRow[] {
        const selectedRows = rows.filter(row => row.medicineId !== null);
        const emptyRow = rows.find(row => row.medicineId === null) ?? this.createEmptyMedicineDraft();
        return [...selectedRows, emptyRow];
    }

    private resetDraftsFromTreatment(treatmentDetail: TreatmentDetailResponse | null): void {
        this.resetTreatmentForm(treatmentDetail);
        this.resetProcedureDrafts(treatmentDetail);
        this.resetMedicineDrafts(treatmentDetail);
    }

    private resetTreatmentForm(treatment: Pick<TreatmentDetailResponse, 'diagnosis' | 'note'> | Pick<TreatmentSummaryResponse, 'diagnosis' | 'note'> | null): void {
        this.treatmentForm.reset({
            diagnosis: treatment?.diagnosis ?? '',
            note: treatment?.note ?? '',
        });
    }

    private resetProcedureDrafts(treatmentDetail: TreatmentDetailResponse | null): void {
        const rows: ProcedureDraftRow[] = (treatmentDetail?.procedures ?? [])
            .filter(item => item.id !== null)
            .map(item => ({
                key: ++this.draftRowKey,
                procedureId: item.id!,
                code: item.code,
                name: item.name,
                unit: 'Lần',
                quantity: item.quantity ?? 1,
                unitPrice: item.unitPrice,
                note: item.note ?? '',
            }));
        this.procedureDrafts.set(this.ensureEmptyProcedureDraft(rows));
    }

    private resetMedicineDrafts(treatmentDetail: TreatmentDetailResponse | null): void {
        this.medicineForm.patchValue({
            advice: treatmentDetail?.prescription?.advice ?? '',
            reExaminationDate: treatmentDetail?.prescription?.reExaminationDate ?? '',
        });
        const rows: MedicineDraftRow[] = (treatmentDetail?.prescription?.items ?? [])
            .filter(item => item.medicineId !== null)
            .map(item => ({
                key: ++this.draftRowKey,
                medicineId: item.medicineId!,
                code: item.medicineCode,
                name: item.medicineName,
                unit: item.medicineUnit,
                quantity: item.quantity ?? 1,
                dosage: item.dosage ?? '',
                frequency: item.frequency ?? '',
                duration: item.duration ?? '',
                instruction: item.instruction ?? '',
            }));
        this.medicineDrafts.set(this.ensureEmptyMedicineDraft(rows));
    }

    private compactProcedureDrafts(): TreatmentProcedureRequest[] {
        return this.procedureDrafts()
            .filter(row => row.procedureId !== null)
            .reduce<TreatmentProcedureRequest[]>((items, row) => this.mergeProcedureRequests(items, {
                procedureId: row.procedureId!,
                quantity: Number(row.quantity) || 1,
                unitPrice: row.unitPrice ?? this.selectedProcedure(row.procedureId)?.price ?? null,
                note: this.emptyToNull(row.note),
            }), []);
    }

    private compactMedicineDrafts(): PrescriptionItemRequest[] {
        return this.medicineDrafts()
            .filter(row => row.medicineId !== null)
            .reduce<PrescriptionItemRequest[]>((items, row) => this.mergePrescriptionItems(items, {
                medicineId: row.medicineId!,
                dosage: this.emptyToNull(row.dosage),
                frequency: this.emptyToNull(row.frequency),
                duration: this.emptyToNull(row.duration),
                quantity: Number(row.quantity) || 1,
                instruction: this.emptyToNull(row.instruction),
            }), []);
    }

    private toProcedureRequests(treatmentDetail: TreatmentDetailResponse | null): TreatmentProcedureRequest[] {
        return (treatmentDetail?.procedures ?? [])
            .filter(item => item.id !== null)
            .map(item => ({
                procedureId: item.id!,
                quantity: item.quantity,
                unitPrice: item.unitPrice,
                note: item.note,
            }));
    }

    private mergeProcedureRequests(
        currentItems: TreatmentProcedureRequest[],
        addedItem: TreatmentProcedureRequest,
    ): TreatmentProcedureRequest[] {
        const existedIndex = currentItems.findIndex(item => item.procedureId === addedItem.procedureId);
        if (existedIndex < 0) {
            return [...currentItems, addedItem];
        }

        return currentItems.map((item, index) => index === existedIndex
            ? {
                ...item,
                quantity: (item.quantity ?? 0) + (addedItem.quantity ?? 0),
                unitPrice: addedItem.unitPrice ?? item.unitPrice,
                note: addedItem.note ?? item.note,
            }
            : item);
    }

    private toPrescriptionRequest(prescription: PrescriptionDetailResponse): PrescriptionRequest {
        return {
            doctorId: prescription.doctor?.id ?? this.dentist()?.id ?? null,
            prescribedAt: prescription.prescribedAt ?? this.toLocalDateTime(new Date()),
            advice: prescription.advice,
            reExaminationDate: prescription.reExaminationDate,
            note: prescription.note,
            items: prescription.items
                .filter(item => item.medicineId !== null)
                .map(item => ({
                    medicineId: item.medicineId!,
                    dosage: item.dosage,
                    frequency: item.frequency,
                    duration: item.duration,
                    quantity: item.quantity,
                    instruction: item.instruction,
                })),
        };
    }

    private mergePrescriptionItems(
        currentItems: PrescriptionItemRequest[],
        addedItem: PrescriptionItemRequest,
    ): PrescriptionItemRequest[] {
        const existedIndex = currentItems.findIndex(item => item.medicineId === addedItem.medicineId);
        if (existedIndex < 0) {
            return [...currentItems, addedItem];
        }

        return currentItems.map((item, index) => index === existedIndex
            ? {
                ...item,
                dosage: addedItem.dosage ?? item.dosage,
                frequency: addedItem.frequency ?? item.frequency,
                duration: addedItem.duration ?? item.duration,
                quantity: (item.quantity ?? 0) + (addedItem.quantity ?? 0),
                instruction: addedItem.instruction ?? item.instruction,
            }
            : item);
    }

    private createEmptyPrescriptionRequest(appointment: AppointmentResponse): PrescriptionRequest {
        return {
            doctorId: appointment.dentistId ?? this.dentist()?.id ?? null,
            prescribedAt: this.toLocalDateTime(new Date()),
            advice: null,
            reExaminationDate: null,
            note: null,
            items: [],
        };
    }

    private refreshPatientDetail(appointment: AppointmentResponse): void {
        this.examinationService.patientDetail(appointment.patientId).subscribe({
            next: res => {
                if (res.data) {
                    this.patientDetails.update(state => ({...state, [appointment.patientId]: res.data}));
                    this.loadTreatmentDetailForAppointment(appointment);
                }
            },
        });
    }

    private runAction(
        appointment: AppointmentResponse,
        action: ExamAction,
        request: () => Observable<SuccessResponse<AppointmentResponse>>,
        successMessage: string,
    ): void {
        this.actionLoading.set(this.actionKey(appointment, action));
        request().subscribe({
            next: () => {
                this.actionLoading.set(null);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: successMessage,
                });
                this.loadAppointments();
            },
            error: (err: HttpErrorResponse) => {
                this.actionLoading.set(null);
                this.showError(err, 'Không thể xử lý thao tác');
            },
        });
    }

    private showError(err: HttpErrorResponse, fallback: string): void {
        const detail = typeof err.error?.message === 'string' ? err.error.message : fallback;
        this.messageService.add({severity: 'error', summary: 'Lỗi', detail});
    }

    private confirmStartDifferentDate(appointment: AppointmentResponse): void {
        this.confirmationService.confirm({
            header: 'Ngày giờ lịch hẹn không phải hôm nay',
            message: `Ngày giờ của lịch hẹn này là <b>${this.formatDateTime(appointment.appointmentDate)}</b>. Hôm nay là <b>${this.formatDateTime(this.toLocalDateTime(new Date()))}</b>. Bạn có muốn tiếp tục bắt đầu khám cho lịch hẹn này không?`,
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Tiếp tục khám',
            rejectLabel: 'Không',
            acceptButtonStyleClass: 'p-button-warning',
            accept: () => this.runStartAppointment(appointment),
        });
    }

    private runStartAppointment(appointment: AppointmentResponse): void {
        this.runAction(
            appointment,
            'start',
            () => this.examinationService.start(appointment.id),
            'Đã chuyển sang đang khám',
        );
    }

    private showWarn(detail: string): void {
        this.messageService.add({severity: 'warn', summary: 'Thiếu thông tin', detail});
    }

    private resetProcedureForm(): void {
        this.procedureForm.reset({
            procedureId: null,
            quantity: 1,
            note: '',
        });
    }

    private resetMedicineForm(): void {
        this.medicineForm.reset({
            medicineId: null,
            dosage: '',
            frequency: '',
            duration: '',
            quantity: 1,
            instruction: '',
            advice: '',
            reExaminationDate: '',
        });
    }

    private emptyToNull(value: string | null | undefined): string | null {
        const trimmed = value?.trim();
        return trimmed ? trimmed : null;
    }

    private actionKey(appointment: AppointmentResponse, action: ExamAction): string {
        return `${appointment.id}:${action}`;
    }

    private isVisibleExamAppointment(appointment: AppointmentResponse, filter: ExamStatusFilter): boolean {
        if (appointment.status === 'CANCELLED' || appointment.status === 'COMPLETED' || appointment.arrivalStatus === 'NO_SHOW') {
            return false;
        }

        if (filter === 'IN_PROGRESS') {
            return appointment.status === 'IN_PROGRESS';
        }

        const waitingForExam = appointment.status === 'CONFIRMED';

        if (filter === 'WAITING') {
            return waitingForExam;
        }

        return waitingForExam || appointment.status === 'IN_PROGRESS';
    }

    private toDateString(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    private toLocalDateTime(date: Date): string {
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hour = String(date.getHours()).padStart(2, '0');
        const minute = String(date.getMinutes()).padStart(2, '0');
        const second = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day}T${hour}:${minute}:${second}`;
    }
}
