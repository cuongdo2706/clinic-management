import {Component, model} from '@angular/core';
import {RouterLink, RouterLinkActive} from "@angular/router";
import {Tooltip} from "primeng/tooltip";

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
    ],
    templateUrl: './sidebar.html',
    styleUrl: './sidebar.css',
})
export class Sidebar {
    collapsed = model<boolean>(false);

    menuItems: MenuItem[] = [
        {label: 'Thống kê', icon: 'pi pi-chart-bar', route: '/thong-ke'},
        {label: 'Lịch hẹn', icon: 'pi pi-calendar', route: '/lich-hen'},
        {label: 'Khám bệnh', icon: 'pi pi-heart-fill', route: '/kham-benh'},
        {label: 'Nha sĩ', icon: 'pi pi-users', route: '/nha-si'},
        {label: 'Bệnh nhân', icon: 'pi pi-user', route: '/benh-nhan'},
        {label: 'Dịch vụ', icon: 'pi pi-list', route: '/dich-vu'},
    ];

    toggleCollapse() {
        this.collapsed.set(!this.collapsed());
    }
}
