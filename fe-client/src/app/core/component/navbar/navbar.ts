import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { Menu } from 'primeng/menu';
import { MenuItem } from 'primeng/api';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, Button, Menu],
  templateUrl: './navbar.html',
  styleUrl: './navbar.css',
})
export class Navbar {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  readonly isLoggedIn = this.authService.isLoggedIn;

  readonly userMenuItems: MenuItem[] = [
    {
      label: 'Thông tin cá nhân',
      icon: 'pi pi-user',
      command: () => this.router.navigate(['/profile']),
    },
    {
      label: 'Đăng xuất',
      icon: 'pi pi-sign-out',
      command: () => this.logout(),
    },
  ];

  logout(): void {
    this.authService.logout();
  }
}
