import {Component, inject, signal} from '@angular/core';
import {AuthService} from "../../service/auth.service";
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
import {FormBuilder, ReactiveFormsModule, Validators} from "@angular/forms";

@Component({
    selector: 'app-login',
    imports: [
        Toast,
        Card,
        FloatLabel,
        InputText,
        Button,
        IconField,
        InputIcon,
        ReactiveFormsModule,
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
    private readonly fb = inject(FormBuilder);
    
    showPassword = signal<boolean>(false);
    
    loginForm = this.fb.nonNullable.group({
        username: ['', Validators.required],
        password: ['', Validators.required],
    });
    
    onLogin() {
        if (this.loginForm.invalid) return;
        
        const value = this.loginForm.getRawValue();
        const payload: LoginRequest = {
            username: value.username,
            password: value.password,
        };
        
        this.authService.login(payload).subscribe({
            next: res => {
                const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/dashboard';
                this.router.navigateByUrl(returnUrl);
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
