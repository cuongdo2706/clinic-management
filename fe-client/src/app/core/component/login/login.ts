import { Component, inject, input, output, signal } from '@angular/core';
import { ReactiveFormsModule, Validators, FormBuilder } from '@angular/forms';
import { Router } from '@angular/router';
import { Button } from 'primeng/button';
import { Dialog } from 'primeng/dialog';
import { IconField } from 'primeng/iconfield';
import { InputIcon } from 'primeng/inputicon';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, Button, Dialog, IconField, InputIcon, InputText, Message],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private static readonly LOGIN_FAILED_MESSAGE = 'Tài khoản hoặc mật khẩu không đúng.';

  private readonly authService = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  readonly returnUrl = input<string | null>(null);
  readonly closed = output<void>();
  readonly showPassword = signal(false);
  readonly loading = signal(false);
  readonly error = signal('');

  readonly loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  onLogin(): void {
    this.error.set('');
    if (this.loginForm.invalid) {
      this.error.set('Vui lòng nhập tài khoản và mật khẩu.');
      return;
    }

    this.loading.set(true);
    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigateByUrl(this.returnUrl() || '/');
      },
      error: () => {
        this.loading.set(false);
        this.error.set(Login.LOGIN_FAILED_MESSAGE);
      },
    });
  }

  close(): void {
    this.closed.emit();
  }

  onVisibleChange(visible: boolean): void {
    if (!visible) {
      this.close();
    }
  }
}
