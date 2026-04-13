import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from "@angular/common";
import {MedicineService} from "../../core/service/medicine.service";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormsModule} from "@angular/forms";
import {Toolbar} from "primeng/toolbar";
import {MedicineResponse} from "../../core/model/response/medicine-response";
import {form, FormField} from "@angular/forms/signals";
import {SearchMedicineRequest} from "../../core/model/request/search-medicine-request";
import {Paginator, PaginatorState} from "primeng/paginator";
import {PageData} from "../../core/model/response/page-data";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tooltip} from "primeng/tooltip";
import {Tag} from "primeng/tag";
import {Dialog} from "primeng/dialog";
import {MedicineSaveForm} from "./medicine-save-form/medicine-save-form";
import {MedicineUpdateForm} from "./medicine-update-form/medicine-update-form";

@Component({
    selector: 'app-medicine',
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
        Tag,
        Dialog,
        MedicineSaveForm,
        MedicineUpdateForm,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './medicine.html',
    styleUrl: './medicine.css',
})
export class Medicine implements OnInit {
    private readonly medicineService = inject(MedicineService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    medicines = signal<PageData<MedicineResponse> | null>(null);
    loading = signal(false);
    exporting = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingMedicine = signal<MedicineResponse | null>(null);

    searchForm = form(signal<SearchMedicineRequest>({
        page: 0,
        size: 10,
        sortBy: "CREATED_AT_DESC",
        codeKeyword: "",
        nameKeyword: "",
    }));

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
        this.medicineService.search(this.searchForm().value()).subscribe({
            next: res => {
                this.medicines.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    onPageChange(event: PaginatorState) {
        this.paginatorFirst.set(event.first!);
        this.loading.set(true);
        const request: SearchMedicineRequest = {
            ...this.searchForm().value(),
            page: event.page!,
            size: event.rows!
        };
        this.medicineService.search(request).subscribe({
            next: res => {
                this.medicines.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }

    toggleRow(medicine: MedicineResponse) {
        const id = medicine.id.toString();
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
        this.medicineService.exportExcel().subscribe({
            next: (blob) => {
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                const today = new Date().toISOString().slice(0, 10);
                a.href = url;
                a.download = `medicines_${today}.xlsx`;
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

    onMedicineSaved(): void {
        this.showAddDialog.set(false);
        this.showEditDialog.set(false);
        this.editingMedicine.set(null);
        this.onSearch();
    }

    openEditDialog(medicine: MedicineResponse): void {
        this.editingMedicine.set(medicine);
        this.showEditDialog.set(true);
    }

    confirmDelete(medicine: MedicineResponse): void {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xóa thuốc <b>${medicine.name}</b> (${medicine.code})?`,
            header: 'Xác nhận xóa',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xóa',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.medicineService.delete(medicine.id.toString()).subscribe({
                    next: () => {
                        this.messageService.add({
                            severity: 'success',
                            summary: 'Thành công',
                            detail: 'Đã xóa thuốc thành công'
                        });
                        this.onSearch();
                    },
                    error: () => {
                        this.messageService.add({
                            severity: 'error',
                            summary: 'Lỗi',
                            detail: 'Không thể xóa thuốc, vui lòng thử lại'
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
        });
        this.paginatorFirst.set(0);
        this.onSearch();
    }
}
