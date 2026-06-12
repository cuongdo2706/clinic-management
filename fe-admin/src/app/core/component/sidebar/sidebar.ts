import {Component, computed, inject, model} from '@angular/core';
import {RouterLink, RouterLinkActive} from "@angular/router";
import {Tooltip} from "primeng/tooltip";
import {AuthService} from "../../service/auth.service";

export interface MenuItem {
    label: string;
    icon: string;
    route?: string;
    requiredRole?: string;
    requiredPermission?: string;
    children?: MenuItem[];
}

@Component({
    selector: 'app-sidebar',
    imports: [
        RouterLink,
        RouterLinkActive,
        Tooltip,
    ],
    templateUrl: './sidebar.html',
    styleUrl: './sidebar.css',
})
export class Sidebar {
    private readonly authService = inject(AuthService);

    collapsed = model<boolean>(false);
    private readonly baseMenuItems: MenuItem[] = [
        {label: 'Tổng quan', icon: 'pi pi-chart-bar', route: '/dashboard', requiredPermission: 'DASHBOARD:VIEW'},
        {label: 'Lịch hẹn', icon: 'pi pi-calendar', route: '/appointments', requiredPermission: 'APPOINTMENT:VIEW'},
        {
            label: 'Khám bệnh',
            icon: 'pi pi-file-edit',
            route: '/examinations',
            requiredRole: 'DENTIST',
            requiredPermission: 'EXAMINATION:VIEW',
        },
        {label: 'Nhân viên', icon: 'pi pi-users', route: '/staffs', requiredPermission: 'STAFF:VIEW'},
        {label: 'Bệnh nhân', icon: 'pi pi-user', route: '/patients', requiredPermission: 'PATIENT:VIEW'},
        {label: 'Dịch vụ', icon: 'pi pi-list', route: '/procedures', requiredPermission: 'PROCEDURE:VIEW'},
        {label: 'Thuốc', icon: 'pi pi-box', route: '/medicines', requiredPermission: 'MEDICINE:VIEW'},
        {label: 'Tài khoản', icon: 'pi pi-user-edit', route: '/accounts', requiredRole: 'ADMIN'},
        {label: 'Phân quyền', icon: 'pi pi-shield', route: '/permissions', requiredRole: 'ADMIN'},
    ];

    menuItems = computed(() => this.baseMenuItems);

    canShow(item: MenuItem): boolean {
        const hasRole = !item.requiredRole || this.authService.hasRole(item.requiredRole);
        const hasPermission = !item.requiredPermission || this.authService.hasPermission(item.requiredPermission);
        if (!hasRole || !hasPermission) {
            return false;
        }
        return !item.children || item.children.some(child => this.canShow(child));
    }

    toggleCollapse() {
        this.collapsed.set(!this.collapsed());
    }
}
