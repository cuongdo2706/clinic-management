import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe, CurrencyPipe} from "@angular/common";
import {ProcedureService} from "../../core/service/procedure.service";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Toolbar} from "primeng/toolbar";
import {ProcedureResponse} from "../../core/model/response/procedure-response";
import {SearchProcedureRequest} from "../../core/model/request/search-procedure-request";
import {Paginator, PaginatorState} from "primeng/paginator";
import {PageData} from "../../core/model/response/page-data";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tooltip} from "primeng/tooltip";
import {Tag} from "primeng/tag";
import {Dialog} from "primeng/dialog";
import {ProcedureSaveForm} from "./procedure-save-form/procedure-save-form";
import {ProcedureUpdateForm} from "./procedure-update-form/procedure-update-form";
import {Select} from "primeng/select";

@Component({
    selector: 'app-procedure',
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
        CurrencyPipe,
        Tooltip,
        Tag,
        Dialog,
        ProcedureSaveForm,
        ProcedureUpdateForm,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './procedure.html',
    styleUrl: './procedure.css',
})
export class Procedure implements OnInit {
    private readonly procedureService = inject(ProcedureService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly fb = inject(FormBuilder);

    procedures = signal<PageData<ProcedureResponse> | null>(null);
    loading = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingProcedure = signal<ProcedureResponse | null>(null);

    searchForm = this.fb.group({
        page: this.fb.nonNullable.control(0),
        size: this.fb.nonNullable.control(10),
        sortBy: this.fb.nonNullable.control("CREATED_AT_DESC"),
        codeKeyword: this.fb.nonNullable.control(""),
        nameKeyword: this.fb.nonNullable.control(""),
        priceFrom: null as number | null,
        priceTo: null as number | null,
    });

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
        this.procedureService.search(this.searchForm.getRawValue()).subscribe({
            next: res => {
                this.procedures.set(res.data);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    onPageChange(event: PaginatorState) {
        this.paginatorFirst.set(event.first!);
        this.loading.set(true);
        const request: SearchProcedureRequest = {
            ...this.searchForm.getRawValue(),
            page: event.page!,
            size: event.rows!,
        };
        this.procedureService.search(request).subscribe({
            next: res => {
                this.procedures.set(res.data);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    toggleRow(procedure: ProcedureResponse) {
        const id = procedure.id.toString();
        const current = this.expandedRows();
        this.expandedRows.set(current[id] ? {} : {[id]: true});
    }

    openAddDialog(): void {
        this.showAddDialog.set(true);
    }

    openEditDialog(procedure: ProcedureResponse): void {
        this.editingProcedure.set(procedure);
        this.showEditDialog.set(true);
    }

    onProcedureSaved(): void {
        this.showAddDialog.set(false);
        this.showEditDialog.set(false);
        this.editingProcedure.set(null);
        this.onSearch();
    }

    confirmDelete(procedure: ProcedureResponse): void {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xóa dịch vụ <b>${procedure.name}</b>?`,
            header: 'Xác nhận xóa',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xóa',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.procedureService.delete(procedure.id).subscribe({
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
        this.searchForm.reset({
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
