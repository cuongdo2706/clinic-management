import {Component, inject, OnInit, signal} from '@angular/core';
import {DatePipe} from "@angular/common";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {Toolbar} from "primeng/toolbar";
import {Select} from "primeng/select";
import {Paginator, PaginatorState} from "primeng/paginator";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tooltip} from "primeng/tooltip";
import {Tag} from "primeng/tag";
import {Dialog} from "primeng/dialog";
import {StaffService} from "../../core/service/staff.service";
import {StaffResponse} from "../../core/model/response/staff-response";
import {PageData} from "../../core/model/response/page-data";
import {SearchStaffRequest} from "../../core/model/request/search-staff-request";
import {StaffSaveForm} from "./staff-save-form/staff-save-form";
import {StaffUpdateForm} from "./staff-update-form/staff-update-form";
import {ENV} from "../../environment";

@Component({
    selector: 'app-staff',
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
        Tag,
        Dialog,
        StaffSaveForm,
        StaffUpdateForm,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './staff.html',
    styleUrl: './staff.css',
})
export class Staff implements OnInit {
    private readonly staffService = inject(StaffService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly fb = inject(FormBuilder);

    staffs = signal<PageData<StaffResponse> | null>(null);
    loading = signal(false);
    paginatorFirst = signal(0);
    expandedRows = signal<{ [key: string]: boolean }>({});
    showAddDialog = signal(false);
    showEditDialog = signal(false);
    editingStaff = signal<StaffResponse | null>(null);

    searchForm = this.fb.group({
        page: this.fb.nonNullable.control(0),
        size: this.fb.nonNullable.control(10),
        sortBy: this.fb.nonNullable.control("CREATED_AT_DESC"),
        codeKeyword: this.fb.nonNullable.control(""),
        nameKeyword: this.fb.nonNullable.control(""),
        phoneKeyword: this.fb.nonNullable.control(""),
        staffType: this.fb.control<string | null>(null),
        isActive: this.fb.control<boolean | null>(true),
    });

    staffTypeOptions = [
        {label: 'Nha sĩ', value: 'DENTIST'},
        {label: 'Lễ tân', value: 'RECEPTIONIST'},
        {label: 'Y tá', value: 'NURSE'},
        {label: 'Quản lý', value: 'ADMIN'},
    ];

    sortOptions = [
        {name: 'Tên: A -> Z', value: "NAME"},
        {name: 'Tên: Z -> A', value: "NAME_DESC"},
        {name: 'Ngày tạo: Gần nhất', value: "CREATED_AT_DESC"},
        {name: 'Ngày tạo: Xa nhất', value: "CREATED_AT"},
    ];

    activeOptions = [
        {label: 'Đang hoạt động', value: true},
        {label: 'Đã ẩn', value: false},
    ];

    private readonly dayLabels: Record<string, string> = {
        MONDAY: 'Thứ 2',
        TUESDAY: 'Thứ 3',
        WEDNESDAY: 'Thứ 4',
        THURSDAY: 'Thứ 5',
        FRIDAY: 'Thứ 6',
        SATURDAY: 'Thứ 7',
        SUNDAY: 'Chủ nhật',
    };

    ngOnInit(): void {
        this.onSearch();
    }

    onSearch(): void {
        this.searchForm.patchValue({page: 0});
        this.paginatorFirst.set(0);
        this.loadStaffs(this.searchForm.getRawValue());
    }

    onPageChange(event: PaginatorState): void {
        const request: SearchStaffRequest = {
            ...this.searchForm.getRawValue(),
            page: event.page!,
            size: event.rows!,
        };
        this.searchForm.patchValue({page: request.page, size: request.size});
        this.paginatorFirst.set(event.first!);
        this.loadStaffs(request);
    }

    toggleRow(staff: StaffResponse): void {
        const id = staff.id.toString();
        const current = this.expandedRows();
        this.expandedRows.set(current[id] ? {} : {[id]: true});
    }

    openAddDialog(): void {
        this.showAddDialog.set(true);
    }

    openEditDialog(staff: StaffResponse): void {
        this.editingStaff.set(staff);
        this.showEditDialog.set(true);
    }

    onStaffSaved(): void {
        this.showAddDialog.set(false);
        this.showEditDialog.set(false);
        this.editingStaff.set(null);
        this.loadStaffs(this.searchForm.getRawValue());
    }

    confirmUpdateStatus(staff: StaffResponse): void {
        const nextActive = !staff.isActive;
        const actionLabel = nextActive ? 'hiện lại' : 'ẩn';
        const successDetail = nextActive ? 'Đã hiện lại nhân viên thành công' : 'Đã ẩn nhân viên thành công';
        const errorDetail = nextActive ? 'Không thể hiện lại nhân viên, vui lòng thử lại' : 'Không thể ẩn nhân viên, vui lòng thử lại';

        this.confirmationService.confirm({
            message: `Bạn có chắc muốn ${actionLabel} nhân viên <b>${staff.fullName}</b> (${staff.code})?`,
            header: nextActive ? 'Xác nhận hiện lại' : 'Xác nhận ẩn',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: nextActive ? 'Hiện lại' : 'Ẩn',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: nextActive ? 'p-button-success' : 'p-button-danger',
            accept: () => {
                this.staffService.updateStatus(staff.id, {isActive: nextActive, version: staff.version}).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: successDetail});
                        this.loadStaffs(this.searchForm.getRawValue());
                    },
                    error: () => {
                        this.messageService.add({severity: 'error', summary: 'Lỗi', detail: errorDetail});
                    }
                });
            }
        });
    }

    onResetFilter(): void {
        this.searchForm.reset({
            page: 0,
            size: 10,
            sortBy: "CREATED_AT_DESC",
            codeKeyword: "",
            nameKeyword: "",
            phoneKeyword: "",
            staffType: null,
            isActive: true,
        });
        this.paginatorFirst.set(0);
        this.loadStaffs(this.searchForm.getRawValue());
    }

    getGenderLabel(gender: boolean | null): string {
        if (gender === true) return 'Nam';
        if (gender === false) return 'Nữ';
        return '—';
    }

    getStaffTypeLabel(staffType: string | null | undefined): string {
        return this.staffTypeOptions.find(option => option.value === staffType)?.label ?? '—';
    }

    getStaffTypeSeverity(staffType: string): 'success' | 'info' | 'warn' | 'secondary' {
        switch (staffType) {
            case 'DENTIST':
                return 'success';
            case 'RECEPTIONIST':
                return 'info';
            case 'NURSE':
                return 'warn';
            default:
                return 'secondary';
        }
    }

    getStatusLabel(isActive: boolean | null | undefined): string {
        return isActive ? 'Hoạt động' : 'Đã ẩn';
    }

    getStatusSeverity(isActive: boolean | null | undefined): 'success' | 'danger' {
        return isActive ? 'success' : 'danger';
    }

    getDayLabel(dayOfWeek: string): string {
        return this.dayLabels[dayOfWeek] ?? dayOfWeek;
    }

    formatTime(time: string): string {
        return time ? time.slice(0, 5) : '—';
    }

    getAvatarUrl(avatarUrl: string | null | undefined): string {
        if (!avatarUrl) return ENV.BASE_IMAGE;
        if (avatarUrl.startsWith('http://') || avatarUrl.startsWith('https://')) return avatarUrl;
        return `${ENV.API_BASE_URL}images/${avatarUrl.replace(/^\/+/, '')}`;
    }

    onImageError(event: Event): void {
        (event.target as HTMLImageElement).src = ENV.BASE_IMAGE;
    }

    private loadStaffs(request: SearchStaffRequest): void {
        this.loading.set(true);
        this.staffService.search(request).subscribe({
            next: res => {
                this.staffs.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách nhân viên'});
            }
        });
    }

}
