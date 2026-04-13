import {Component, model} from '@angular/core';
import {RouterLink, RouterLinkActive} from "@angular/router";
import {Tooltip} from "primeng/tooltip";
import {NgOptimizedImage} from "@angular/common";

export interface MenuItem {
    label: string;
    icon: string;
    route: string;
}

@Component({
    selector: 'app-sidebar',
    imports: [
        RouterLink,
        RouterLinkActive,
        Tooltip,
        NgOptimizedImage,
    ],
    templateUrl: './sidebar.html',
    styleUrl: './sidebar.css',
})
export class Sidebar {
    collapsed = model<boolean>(false);

    menuItems: MenuItem[] = [
        {label: 'Thống kê', icon: 'pi pi-chart-bar', route: '/dashboard'},
        {label: 'Lịch hẹn', icon: 'pi pi-calendar', route: '/appointments'},
        {label: 'Nhân viên', icon: 'pi pi-users', route: '/staffs'},
        {label: 'Bệnh nhân', icon: 'pi pi-user', route: '/patients'},
        {label: 'Dịch vụ', icon: 'pi pi-list', route: '/services'},
        {label: 'Thuốc', icon: 'pi pi-box', route: '/medicines'},
        {label: 'Phân quyền', icon: 'pi pi-shield', route: '/permissions'},
    ];

    toggleCollapse() {
        this.collapsed.set(!this.collapsed());
    }
}
