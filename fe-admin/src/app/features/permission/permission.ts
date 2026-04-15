import {Component, inject, OnInit, signal} from '@angular/core';
import {PermissionService} from "../../core/service/permission.service";
import {RoleService} from "../../core/service/role.service";
import {MessageService} from "primeng/api";
import {Toast} from "primeng/toast";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {ProgressSpinner} from "primeng/progressspinner";
import {Tag} from "primeng/tag";
import {UpdatePermissionRequest} from "../../core/model/request/update-permission-request";
import {Checkbox} from "primeng/checkbox";
import {FormsModule} from "@angular/forms";
import {RoleResponse} from "../../core/model/response/role-response";
import {PagePermission} from "../../core/model/response/permission-response";

interface MatrixRow {
    pageCode: string;
    pageName: string;
    granted: { [actionCode: string]: boolean | null };
}

@Component({
    selector: 'app-permission',
    imports: [
        Toast,
        TableModule,
        Button,
        ProgressSpinner,
        Tag,
        Checkbox,
        FormsModule,
    ],
    providers: [MessageService],
    templateUrl: './permission.html',
    styleUrl: './permission.css',
})
export class Permission implements OnInit {
    private readonly roleService = inject(RoleService);
    private readonly permissionService = inject(PermissionService);
    private readonly messageService = inject(MessageService);

    roles = signal<RoleResponse[]>([]);
    loading = signal(false);
    matrixLoading = signal(false);
    saving = signal(false);
    expandedRows = signal<{ [key: string]: boolean }>({});

    // Matrix data cho role đang mở
    matrixActions = signal<string[]>([]);
    matrixRows = signal<MatrixRow[]>([]);
    editingRoleId = signal<number | null>(null);

    // Snapshot để khôi phục khi hủy
    private matrixSnapshot: MatrixRow[] = [];

    private readonly actionLabels: { [key: string]: string } = {
        'VIEW': 'Xem',
        'CREATE': 'Thêm',
        'UPDATE': 'Sửa',
        'DELETE': 'Xóa',
        'EXPORT': 'Xuất',
    };

    ngOnInit() {
        this.loadRoles();
    }

    // ─── Load danh sách vai trò ──────────────────────────────

    loadRoles() {
        this.loading.set(true);
        this.roleService.findAll().subscribe({
            next: res => {
                this.roles.set(res.data);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải danh sách vai trò'});
            }
        });
    }

    // ─── Mở / đóng expanded row ─────────────────────────────

    toggleRow(item: RoleResponse) {
        if (this.editingRoleId() !== null) return;

        const id = item.id.toString();
        const current = this.expandedRows();
        if (current[id]) {
            this.expandedRows.set({});
            this.matrixActions.set([]);
            this.matrixRows.set([]);
        } else {
            this.expandedRows.set({[id]: true});
            this.loadMatrix(item);
        }
    }

    // ─── Load ma trận quyền từ API ──────────────────────────

    private loadMatrix(item: RoleResponse) {
        this.matrixLoading.set(true);
        this.permissionService.findByRoleId(item.id).subscribe({
            next: res => {
                const data = res.data;
                this.matrixActions.set(data.actions);
                this.matrixRows.set(this.buildMatrix(data.actions, data.pages));
                this.matrixLoading.set(false);
            },
            error: () => {
                this.matrixLoading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải ma trận quyền'});
            }
        });
    }

    /**
     * Xây dựng matrix rows từ API data.
     * Mỗi page từ API đã có granted map (chỉ chứa actions áp dụng cho page đó).
     * Với các actions không có trong granted → set null (hiển thị "—").
     */
    private buildMatrix(actions: string[], pages: PagePermission[]): MatrixRow[] {
        return pages.map(page => {
            const granted: { [key: string]: boolean | null } = {};
            for (const action of actions) {
                granted[action] = action in page.granted ? page.granted[action] : null;
            }
            return {pageCode: page.pageCode, pageName: page.pageName, granted};
        });
    }

    // ─── Edit mode ──────────────────────────────────────────

    enterEditMode(item: RoleResponse) {
        this.matrixSnapshot = this.matrixRows().map(row => ({
            pageCode: row.pageCode,
            pageName: row.pageName,
            granted: {...row.granted}
        }));
        this.editingRoleId.set(item.id);
    }

    cancelEdit() {
        this.matrixRows.set(this.matrixSnapshot);
        this.editingRoleId.set(null);
    }

    // ─── Checkbox thay đổi ──────────────────────────────────

    onCheckboxChange(pageCode: string, actionCode: string, checked: boolean) {
        this.matrixRows.update(rows => rows.map(row =>
            row.pageCode === pageCode
                ? {...row, granted: {...row.granted, [actionCode]: checked}}
                : row
        ));
    }

    // ─── Select all theo cột (action) ───────────────────────

    isColumnAllChecked(actionCode: string): boolean {
        return this.matrixRows().every(row =>
            row.granted[actionCode] === null || row.granted[actionCode] === true
        );
    }

    toggleColumn(actionCode: string, checked: boolean) {
        this.matrixRows.update(rows => rows.map(row =>
            row.granted[actionCode] !== null
                ? {...row, granted: {...row.granted, [actionCode]: checked}}
                : row
        ));
    }

    // ─── Select all theo hàng (page) ────────────────────────

    isRowAllChecked(pageCode: string): boolean {
        const row = this.matrixRows().find(r => r.pageCode === pageCode);
        if (!row) return false;
        return this.matrixActions().every(action =>
            row.granted[action] === null || row.granted[action] === true
        );
    }

    toggleRowAll(pageCode: string, checked: boolean) {
        this.matrixRows.update(rows => rows.map(row => {
            if (row.pageCode !== pageCode) return row;
            const newGranted = {...row.granted};
            for (const action of this.matrixActions()) {
                if (newGranted[action] !== null) {
                    newGranted[action] = checked;
                }
            }
            return {...row, granted: newGranted};
        }));
    }

    // ─── Select all toàn bộ matrix ──────────────────────────

    isAllChecked(): boolean {
        return this.matrixRows().every(row =>
            this.matrixActions().every(action =>
                row.granted[action] === null || row.granted[action] === true
            )
        );
    }

    toggleAll(checked: boolean) {
        this.matrixRows.update(rows => rows.map(row => {
            const newGranted = {...row.granted};
            for (const action of this.matrixActions()) {
                if (newGranted[action] !== null) {
                    newGranted[action] = checked;
                }
            }
            return {...row, granted: newGranted};
        }));
    }

    // ─── Lưu quyền ─────────────────────────────────────────

    savePermissions(item: RoleResponse) {
        const permissions: { pageCode: string; actionCode: string }[] = [];
        for (const row of this.matrixRows()) {
            for (const action of this.matrixActions()) {
                if (row.granted[action] === true) {
                    permissions.push({pageCode: row.pageCode, actionCode: action});
                }
            }
        }

        const request: UpdatePermissionRequest = {
            roleId: item.id,
            permissions
        };

        this.saving.set(true);
        this.permissionService.update(request).subscribe({
            next: () => {
                this.saving.set(false);
                this.editingRoleId.set(null);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật quyền thành công'});
                this.loadMatrix(item);
            },
            error: () => {
                this.saving.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật quyền, vui lòng thử lại'});
            }
        });
    }

    // ─── Label helpers ──────────────────────────────────────

    getActionLabel(code: string): string {
        return this.actionLabels[code] || code;
    }
}
