import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe, CurrencyPipe} from "@angular/common";
import {TreatmentService} from "../../core/service/treatment.service";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormsModule} from "@angular/forms";
import {Toolbar} from "primeng/toolbar";
import {TreatmentResponse} from "../../core/model/response/treatment-response";
import {form, FormField} from "@angular/forms/signals";
import {SearchTreatmentRequest} from "../../core/model/request/search-treatment-request";
import {Paginator, PaginatorState} from "primeng/paginator";
import {PageData} from "../../core/model/response/page-data";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tooltip} from "primeng/tooltip";
import {Tag} from "primeng/tag";
import {Dialog} from "primeng/dialog";
import {TreatmentSaveForm} from "./treatment-save-form/treatment-save-form";
import {TreatmentUpdateForm} from "./treatment-update-form/treatment-update-form";

@Component({
    selector: 'app-treatment',
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
        CurrencyPipe,
        Tooltip,
        Tag,
        Dialog,
        TreatmentSaveForm,
        TreatmentUpdateForm,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './treatment.html',
    styleUrl: './treatment.css',
})
export class Treatment implements OnInit {
    private readonly treatmentService = inject(TreatmentService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    treatments = signal<PageData<TreatmentResponse> | null>(null);
    loading = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingTreatment = signal<TreatmentResponse | null>(null);

    searchForm = form(signal<SearchTreatmentRequest>({
        page: 0,
        size: 10,
        sortBy: "CREATED_AT_DESC",
        codeKeyword: "",
        nameKeyword: "",
        priceFrom: null,
        priceTo: null,
    }));

    sortOptions = signal([
        {name: 'Tên: A -> Z', value: "NAME"},
        {name: 'Tên: Z -> A', value: "NAME_DESC"},
        {name: 'Giá: Tăng dần', value: "PRICE"},
        {name: 'Giá: Giảm dần', value: "PRICE_DESC"},
        {name: 'Ngày tạo: Gần nhất', value: "CREATED_AT_DESC"},
        {name: 'Ngày tạo: Xa nhất', value: "CREATED_AT"},
    ]);

    ngOnInit() {
        this.onSearch();
    }

    onSearch() {
        this.loading.set(true);
        this.treatmentService.search(this.searchForm().value()).subscribe({
            next: res => {
                this.treatments.set(res.data);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onPageChange(event: PaginatorState) {
        this.paginatorFirst.set(event.first!);
        this.loading.set(true);
        const request: SearchTreatmentRequest = {
            ...this.searchForm().value(),
            page: event.page!,
            size: event.rows!,
        };
        this.treatmentService.search(request).subscribe({
            next: res => {
                this.treatments.set(res.data);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    toggleRow(treatment: TreatmentResponse) {
        const id = treatment.id.toString();
        const current = this.expandedRows();
        this.expandedRows.set(current[id] ? {} : {[id]: true});
    }

    openAddDialog(): void {
        this.showAddDialog.set(true);
    }

    openEditDialog(treatment: TreatmentResponse): void {
        this.editingTreatment.set(treatment);
        this.showEditDialog.set(true);
    }

    onTreatmentSaved(): void {
        this.showAddDialog.set(false);
        this.showEditDialog.set(false);
        this.editingTreatment.set(null);
        this.onSearch();
    }

    confirmDelete(treatment: TreatmentResponse): void {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xóa dịch vụ <b>${treatment.name}</b>?`,
            header: 'Xác nhận xóa',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xóa',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.treatmentService.delete(treatment.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xóa dịch vụ thành công'});
                        this.onSearch();
                    },
                    error: () => {
                        this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa dịch vụ, vui lòng thử lại'});
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
            priceFrom: null,
            priceTo: null,
        });
        this.paginatorFirst.set(0);
        this.onSearch();
    }
}
