import {Component, inject, signal} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {form, FormField, required} from "@angular/forms/signals";
import {Toast} from "primeng/toast";
import {Card} from "primeng/card";
import {FloatLabel} from "primeng/floatlabel";
import {InputText} from "primeng/inputtext";
import {IconField} from "primeng/iconfield";
import {InputIcon} from "primeng/inputicon";
import {Button} from "primeng/button";
import {LoginRequest} from "../../model/request/login-request";
import {MessageService} from "primeng/api";
import {HttpErrorResponse} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";
import {FormsModule} from "@angular/forms";

interface LoginData {
    username: string;
    password: string;
}

@Component({
    selector: 'app-login',
    imports: [
        Toast,
        Card,
        FloatLabel,
        InputText,
        Button,
        FormField,
        IconField,
        InputIcon,
        FormsModule,
    ],
    templateUrl: './login.html',
    providers: [MessageService],
    styleUrl: './login.css',
})


export class Login {
    private readonly authService = inject(AuthService);
    private readonly messageService = inject(MessageService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    
    showPassword = signal<boolean>(false);
    
    loginForm = form(
            signal<LoginData>({
                username: '',
                password: ''
            }),
            (f) => {
                required(f.username);
                required(f.password);
            }
    );
    
    onLogin() {
        if (this.loginForm().invalid()) return;
        
        const value = this.loginForm().value();
        const payload: LoginRequest = {
            username: value.username as string,
            password: value.password as string,
        };
        
        this.authService.login(payload).subscribe({
            next: res => {
                const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/thong-ke';
                this.router.navigateByUrl(returnUrl);
                console.log(res);
            },
            error: (err: HttpErrorResponse) => {
                console.log(err.error);
                this.messageService.add({
                    severity: 'error',
                    summary: "Đăng nhập thất bại",
                    detail: "Tài khoản hoặc mật khẩu không đúng!"
                });
            }
        });
    }
}
