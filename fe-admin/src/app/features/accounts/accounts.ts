import {DatePipe} from '@angular/common';
import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {ConfirmationService, MessageService} from 'primeng/api';
import {Button} from 'primeng/button';
import {ConfirmDialog} from 'primeng/confirmdialog';
import {Dialog} from 'primeng/dialog';
import {InputText} from 'primeng/inputtext';
import {Paginator, PaginatorState} from 'primeng/paginator';
import {ProgressSpinner} from 'primeng/progressspinner';
import {Select, SelectChangeEvent} from 'primeng/select';
import {TableModule} from 'primeng/table';
import {Tag} from 'primeng/tag';
import {Toast} from 'primeng/toast';
import {Toolbar} from 'primeng/toolbar';
import {Tooltip} from 'primeng/tooltip';
import {AccountResponse, AccountStatus, AccountType} from '../../core/model/account';
import {PageData} from '../../core/model/response/page-data';
import {RoleResponse} from '../../core/model/response/role-response';
import {AccountService} from '../../core/service/account.service';
import {RoleService} from '../../core/service/role.service';

@Component({
    selector: 'app-accounts',
    imports: [
        Button,
        ConfirmDialog,
        DatePipe,
        Dialog,
        FormsModule,
        InputText,
        Paginator,
        ProgressSpinner,
        Select,
        TableModule,
        Tag,
        Toast,
        Toolbar,
        Tooltip,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './accounts.html',
    styleUrl: './accounts.css',
})
export class Accounts implements OnInit {
    private readonly accountService = inject(AccountService);
    private readonly roleService = inject(RoleService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);
    private readonly router = inject(Router);

    accounts = signal<PageData<AccountResponse> | null>(null);
    roleOptions = signal<{ label: string; value: string }[]>([]);
    loading = signal(false);
    activeTab = signal<AccountType>('STAFF');
    paginatorFirst = signal(0);
    resetDialogVisible = signal(false);
    resetAccount = signal<AccountResponse | null>(null);
    temporaryPassword = signal('');

    keyword = '';
    roleCode: string | null = null;
    status: AccountStatus | null = null;

    readonly statusOptions = [
        {label: 'Đang hoạt động', value: 'ACTIVE'},
        {label: 'Bị khóa', value: 'LOCKED'},
        {label: 'Đã vô hiệu hóa', value: 'DISABLED'},
    ];

    ngOnInit(): void {
        this.loadRoles();
        this.loadAccounts();
    }

    visibleAccounts(): AccountResponse[] {
        return this.accounts()?.content ?? [];
    }

    setTab(tab: AccountType): void {
        this.activeTab.set(tab);
        this.keyword = '';
        this.roleCode = null;
        this.status = null;
        this.paginatorFirst.set(0);
        this.loadAccounts();
    }

    search(): void {
        this.paginatorFirst.set(0);
        this.loadAccounts();
    }

    resetFilter(): void {
        this.keyword = '';
        this.roleCode = null;
        this.status = null;
        this.paginatorFirst.set(0);
        this.loadAccounts();
    }

    onPageChange(event: PaginatorState): void {
        this.paginatorFirst.set(event.first ?? 0);
        this.loadAccounts(event.page ?? 0, event.rows ?? this.accounts()?.size ?? 10);
    }

    toggleLock(account: AccountResponse): void {
        const nextStatus: AccountStatus = account.status === 'LOCKED' ? 'ACTIVE' : 'LOCKED';
        this.accountService.updateStatus(account.id, nextStatus).subscribe({
            next: () => {
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: nextStatus === 'LOCKED' ? 'Đã khóa tài khoản' : 'Đã mở khóa tài khoản',
                });
                this.reloadCurrentPage();
            },
            error: () => this.showError('Không thể cập nhật trạng thái tài khoản'),
        });
    }

    updateRole(account: AccountResponse, event: SelectChangeEvent): void {
        const roleCode = event.value;
        if (!roleCode || roleCode === account.roleCode) return;
        this.accountService.updateRole(account.id, roleCode).subscribe({
            next: () => {
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật vai trò'});
                this.reloadCurrentPage();
            },
            error: () => {
                this.showError('Không thể cập nhật vai trò');
                this.reloadCurrentPage();
            },
        });
    }

    openResetPassword(account: AccountResponse): void {
        this.resetAccount.set(account);
        this.temporaryPassword.set('');
        this.resetDialogVisible.set(true);
    }

    confirmResetPassword(): void {
        const account = this.resetAccount();
        if (!account) return;
        this.accountService.resetPassword(account.id).subscribe({
            next: res => {
                this.temporaryPassword.set(res.data.temporaryPassword);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã reset mật khẩu tạm thời'});
                this.reloadCurrentPage();
            },
            error: () => this.showError('Không thể reset mật khẩu'),
        });
    }

    confirmDelete(account: AccountResponse): void {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xóa tài khoản <b>${account.username}</b>? Hồ sơ liên kết vẫn được giữ lại.`,
            header: 'Xác nhận xóa tài khoản',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xóa',
            rejectLabel: 'Hủy',
            acceptButtonStyleClass: 'p-button-danger',
            accept: () => {
                this.accountService.delete(account.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xóa tài khoản'});
                        this.reloadCurrentPage();
                    },
                    error: () => this.showError('Không thể xóa tài khoản'),
                });
            },
        });
    }



    getStatusLabel(status: AccountStatus): string {
        switch (status) {
            case 'ACTIVE':
                return 'Đang hoạt động';
            case 'LOCKED':
                return 'Bị khóa';
            case 'DISABLED':
                return 'Đã vô hiệu hóa';
        }
    }

    getStatusSeverity(status: AccountStatus): 'success' | 'danger' | 'warn' {
        switch (status) {
            case 'ACTIVE':
                return 'success';
            case 'LOCKED':
                return 'danger';
            case 'DISABLED':
                return 'warn';
        }
    }

    getOwnerLabel(): string {
        return this.activeTab() === 'STAFF' ? 'Nhân viên' : 'Khách hàng';
    }

    getContact(account: AccountResponse): string {
        return [account.phone, account.email].filter(Boolean).join(' | ') || '—';
    }

    private loadRoles(): void {
        this.roleService.findAssignable().subscribe({
            next: res => {
                this.roleOptions.set(res.data
                    .filter((role: RoleResponse) => role.code !== 'PATIENT')
                    .map((role: RoleResponse) => ({
                        label: role.name,
                        value: role.code,
                    })));
            },
            error: () => this.showError('Không thể tải danh sách vai trò'),
        });
    }

    private loadAccounts(page = 0, size = this.accounts()?.size ?? 10): void {
        this.loading.set(true);
        this.accountService.search({
            page,
            size,
            type: this.activeTab(),
            keyword: this.keyword,
            roleCode: this.activeTab() === 'STAFF' ? this.roleCode : null,
            status: this.status,
        }).subscribe({
            next: res => {
                this.accounts.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.showError('Không thể tải danh sách tài khoản');
            },
        });
    }

    private reloadCurrentPage(): void {
        const size = this.accounts()?.size ?? 10;
        const page = Math.floor(this.paginatorFirst() / size);
        this.loadAccounts(page, size);
    }

    private showError(detail: string): void {
        this.messageService.add({severity: 'error', summary: 'Lỗi', detail});
    }
}
