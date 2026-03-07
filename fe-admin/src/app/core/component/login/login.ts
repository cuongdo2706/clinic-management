import {Component, inject, signal} from '@angular/core';
import {AuthService} from "../../service/auth.service";
import {form, FormField} from "@angular/forms/signals";
import {Toast} from "primeng/toast";
import {Card} from "primeng/card";
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {FloatLabel} from "primeng/floatlabel";
import {InputText} from "primeng/inputtext";
import {Password} from "primeng/password";
import {Button} from "primeng/button";
import {LoginRequest} from "../../model/request/login-request";
import {MessageService} from "primeng/api";
import {HttpErrorResponse} from "@angular/common/http";
import {ActivatedRoute, Router} from "@angular/router";


@Component({
    selector: 'app-login',
    imports: [
        Toast,
        Card,
        FormsModule,
        FloatLabel,
        InputText,
        Button,
        ReactiveFormsModule,
        Password
    ],
    templateUrl: './login.html',
    providers: [MessageService],
    styleUrl: './login.css',
})


export class Login {
    private readonly authService = inject(AuthService);
    private readonly fb: FormBuilder = inject(FormBuilder);
    private readonly messageService = inject(MessageService);
    private readonly router = inject(Router);
    private readonly route = inject(ActivatedRoute);
    
    loginForm = this.fb.group({
        username: ["", Validators.required],
        password: ["", Validators.required],
    });
    
    onLogin() {
        this.authService.login(<LoginRequest>this.loginForm.value).subscribe({
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
