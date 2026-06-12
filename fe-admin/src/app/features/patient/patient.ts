import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from "@angular/common";
import {PatientService} from "../../core/service/patient.service";
import {TreatmentService} from "../../core/service/treatment.service";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Toolbar} from "primeng/toolbar";
import {Select} from "primeng/select";
import {PatientResponse} from "../../core/model/response/patient-response";
import {SearchPatientRequest} from "../../core/model/request/search-patient-request";
import {Paginator, PaginatorState} from "primeng/paginator";
import {PageData} from "../../core/model/response/page-data";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tooltip} from "primeng/tooltip";
import {Tabs, TabList, Tab, TabPanels, TabPanel} from "primeng/tabs";
import {Tag} from "primeng/tag";
import {Dialog} from "primeng/dialog";
import {PatientSaveForm} from "./patient-save-form/patient-save-form";
import {PatientUpdateForm} from "./patient-update-form/patient-update-form";
import {
    PatientDetailResponse,
    PrescriptionDetailResponse,
    TreatmentDetailResponse,
    TreatmentStatus,
    TreatmentSummaryResponse
} from "../../core/model/response/patient-detail-response";
import {AppointmentStatus} from "../appointment/model/appointment.model";

@Component({
    selector: 'app-patient',
    imports: [
        Toast,
        ConfirmDialog,
        TableModule,
        Button,
        InputText,
        FloatLabel,
        ReactiveFormsModule,
        Toolbar,
        Select,
        Paginator,
        ProgressSpinner,
        DatePipe,
        Tooltip,
        Tabs,
        TabList,
        Tab,
        TabPanels,
        TabPanel,
        Tag,
        Dialog,
        PatientSaveForm,
        PatientUpdateForm,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './patient.html',
    styleUrl: './patient.css',
})
export class Patient implements OnInit {
    private readonly patientService = inject(PatientService);
    private readonly treatmentService = inject(TreatmentService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly fb = inject(FormBuilder);
    
    patients = signal<PageData<PatientResponse> | null>(null);
    loading = signal(false);
    exporting = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    patientDetails = signal<Record<number, PatientDetailResponse>>({});
    detailLoading = signal<Record<number, boolean>>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingPatient = signal<PatientResponse | null>(null);
    showTreatmentDetailDialog = signal(false);
    showPrescriptionDetailDialog = signal(false);
    selectedTreatment = signal<TreatmentDetailResponse | null>(null);
    selectedPrescription = signal<PrescriptionDetailResponse | null>(null);
    
    searchForm = this.fb.group({
        page: this.fb.nonNullable.control(0),
        size: this.fb.nonNullable.control(10),
        sortBy: this.fb.nonNullable.control("CREATED_AT_DESC"),
        codeKeyword: this.fb.nonNullable.control(""),
        nameKeyword: this.fb.nonNullable.control(""),
        phoneKeyword: this.fb.nonNullable.control(""),
        guardianNameKeyword: this.fb.nonNullable.control(""),
        guardianPhoneKeyword: this.fb.nonNullable.control("")
    });
    
    genderOptions = [
        {label: 'Nam', value: true},
        {label: 'Nữ', value: false},
    ];
    
    sortOptions = signal([
        {name: 'Tên: A -> Z', value: "NAME"},
        {name: 'Tên: Z -> A', value: "NAME_DESC"},
        {name: 'Ngày tạo: Gần nhất', value: "CREATED_AT_DESC"},
        {name: 'Ngày tạo: Xa nhất', value: "CREATED_AT"}
    ]);
    
    ngOnInit() {
        this.onSearch();
    }
    
    onSearch() {
        this.loading.set(true);
        this.patientService.search(this.searchForm.getRawValue()).subscribe({
            next: res => {
                this.patients.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
    
    getGenderLabel(gender: boolean | null | undefined): string {
        if (gender === null || gender === undefined) {
            return '—';
        }
        return gender ? 'Nam' : 'Nữ';
    }
    
    onPageChange(event: PaginatorState) {
        this.paginatorFirst.set(event.first!);
        this.loading.set(true);
        const request: SearchPatientRequest = {
            ...this.searchForm.getRawValue(),
            page: event.page!,
            size: event.rows!
        };
        this.patientService.search(request).subscribe({
            next: res => {
                this.patients.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
    toggleRow(patient: PatientResponse) {
        const id = patient.id.toString();
        const current = this.expandedRows();
        if (current[id]) {
            this.expandedRows.set({});
        } else {
            this.expandedRows.set({[id]: true});
            this.loadPatientDetail(patient.id);
        }
    }

    loadPatientDetail(patientId: number): void {
        if (this.patientDetails()[patientId] || this.detailLoading()[patientId]) {
            return;
        }
        this.detailLoading.update(state => ({...state, [patientId]: true}));
        this.patientService.findDetail(patientId).subscribe({
            next: res => {
                this.patientDetails.update(state => ({...state, [patientId]: res.data}));
                this.detailLoading.update(state => ({...state, [patientId]: false}));
            },
            error: () => {
                this.detailLoading.update(state => ({...state, [patientId]: false}));
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải chi tiết bệnh nhân'
                });
            }
        });
    }

    patientDetail(patientId: number): PatientDetailResponse | null {
        return this.patientDetails()[patientId] ?? null;
    }

    isDetailLoading(patientId: number): boolean {
        return !!this.detailLoading()[patientId];
    }

    openTreatmentDetail(treatment: TreatmentSummaryResponse): void {
        this.treatmentService.findById(treatment.id).subscribe({
            next: res => {
                this.selectedTreatment.set(res.data);
                this.showTreatmentDetailDialog.set(true);
            },
            error: () => {
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải chi tiết phiếu điều trị'
                });
            }
        });
    }

    openPrescriptionDetail(prescription: PrescriptionDetailResponse): void {
        this.selectedPrescription.set(prescription);
        this.showPrescriptionDetailDialog.set(true);
    }

    printPrescription(prescription: PrescriptionDetailResponse | null): void {
        if (!prescription) {
            return;
        }
        this.selectedPrescription.set(prescription);
        this.showPrescriptionDetailDialog.set(true);
        setTimeout(() => window.print(), 120);
    }

    getTreatmentStatusLabel(status: TreatmentStatus | null | undefined): string {
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

    getTreatmentStatusSeverity(status: TreatmentStatus | null | undefined): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
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

    getAppointmentStatusLabel(status: AppointmentStatus | null | undefined): string {
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

    getAppointmentStatusSeverity(status: AppointmentStatus | null | undefined): 'success' | 'info' | 'warn' | 'danger' | 'secondary' {
        switch (status) {
            case 'PENDING':
                return 'warn';
            case 'CONFIRMED':
                return 'info';
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

    getActiveLabel(active: boolean | null | undefined): string {
        return active === false ? 'Ngừng hoạt động' : 'Hoạt động';
    }

    openAddDialog(): void {
        this.showAddDialog.set(true);
    }

    onExportExcel(): void {
        this.exporting.set(true);
        this.patientService.exportExcel().subscribe({
            next: (blob) => {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                const today = new Date().toISOString().slice(0, 10);
                a.href = url;
                a.download = `patients_${today}.xlsx`;
                a.click();
                URL.revokeObjectURL(url);
                this.exporting.set(false);
            },
            error: () => {
                this.exporting.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể xuất Excel, vui lòng thử lại'
                });
            }
        });
    }

    onPatientSaved(): void {
        this.showAddDialog.set(false);
        this.showEditDialog.set(false);
        this.editingPatient.set(null);
        this.onSearch();
    }

    openEditDialog(patient: PatientResponse): void {
        this.editingPatient.set(patient);
        this.showEditDialog.set(true);
    }

    confirmDelete(patient: PatientResponse): void {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xóa bệnh nhân <b>${patient.fullName}</b> (${patient.code})?`,
            header: 'Xác nhận xóa',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xóa',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.patientService.delete(patient.id.toString()).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Thành công',
                            detail: 'Đã xóa bệnh nhân thành công'
                        });
                        this.onSearch();
                    },
                    error: () => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Lỗi',
                            detail: 'Không thể xóa bệnh nhân, vui lòng thử lại'
                        });
                    }
                });
            }
        });
    }
    
    onResetFilter() {
        this.searchForm.reset({
            page: 0,
            size: 10,
            sortBy: "CREATED_AT_DESC",
            codeKeyword: "",
            nameKeyword: "",
            phoneKeyword: "",
            guardianPhoneKeyword: "",
            guardianNameKeyword: ""
        });
        this.paginatorFirst.set(0);
        this.onSearch();
    }
}
