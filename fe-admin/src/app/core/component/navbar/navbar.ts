import {Component, inject} from '@angular/core';
import {Button} from "primeng/button";
import {AuthService} from "../../service/auth.service";

@Component({
    selector: 'app-navbar',
    imports: [
        Button
    ],
    templateUrl: './navbar.html',
    styleUrl: './navbar.css',
})
export class Navbar {
    private readonly authService = inject(AuthService);
    logout(){
         this.authService.logout();
    }
}
