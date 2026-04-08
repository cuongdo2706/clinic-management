import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from "@angular/common";
import {PatientService} from "./service/patient.service";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormsModule} from "@angular/forms";
import {Toolbar} from "primeng/toolbar";
import {Select} from "primeng/select";
import {PatientResponse} from "../../core/model/response/patient-response";
import {form, FormField} from "@angular/forms/signals";
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

@Component({
    selector: 'app-patient',
    imports: [
        Toast,
        ConfirmDialog,
        TableModule,
        Button,
        InputText,
        FloatLabel,
        FormsModule,
        Toolbar,
        FormField,
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
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    
    patients = signal<PageData<PatientResponse> | null>(null);
    loading = signal(false);
    exporting = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingPatient = signal<PatientResponse | null>(null);
    
    searchForm = form(signal<SearchPatientRequest>(
            {
                page: 0,
                size: 10,
                sortBy: "CREATED_AT_DESC",
                codeKeyword: "",
                nameKeyword: "",
                phoneKeyword: "",
                guardianNameKeyword: "",
                guardianPhoneKeyword: ""
            }
    ));
    
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
        this.patientService.search(this.searchForm().value()).subscribe({
            next: res => {
                this.patients.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }
    
    getGenderLabel(gender: string): string {
        return gender ? 'Nam' : 'Nữ';
    }
    
    onPageChange(event: PaginatorState) {
        this.paginatorFirst.set(event.first!);
        this.loading.set(true);
        const request: SearchPatientRequest = {
            ...this.searchForm().value(),
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
        }
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
        this.searchForm().reset({
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
