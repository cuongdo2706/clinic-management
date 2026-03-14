import {Component, inject} from '@angular/core';
import {Button} from "primeng/button";
import {AuthService} from "../../service/auth.service";
import {Menu} from "primeng/menu";
import {MenuItem} from "primeng/api";

@Component({
    selector: 'app-navbar',
    imports: [
        Button,
        Menu,
    ],
    templateUrl: './navbar.html',
    styleUrl: './navbar.css',
})
export class Navbar {
    private readonly authService = inject(AuthService);

    userMenuItems: MenuItem[] = [
        {
            label: 'Đăng xuất',
            icon: 'pi pi-sign-out',
            command: () => this.logout(),
        }
    ];

    logout() {
        this.authService.logout();
    }
}
