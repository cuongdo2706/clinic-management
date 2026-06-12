import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface MenuItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  readonly menuItems: MenuItem[] = [
    { label: 'Đặt lịch khám', icon: 'pi pi-calendar', route: '/booking' },
    { label: 'Quản lý lịch khám', icon: 'pi pi-list', route: '/appointments' },
    { label: 'Hồ sơ sức khỏe', icon: 'pi pi-heart', route: '/health-records' },
  ];
}
