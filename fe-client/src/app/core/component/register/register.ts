import { Component, inject, output, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs';
import { Button } from 'primeng/button';
import { DatePicker } from 'primeng/datepicker';
import { Dialog } from 'primeng/dialog';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { AuthService } from '../../service/auth.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, Button, DatePicker, Dialog, InputText, Message, Select, Textarea],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private static readonly REGISTER_FAILED_MESSAGE = 'Không thể đăng ký tài khoản. Vui lòng kiểm tra thông tin.';

  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  readonly closed = output<void>();
  readonly loading = signal(false);
  readonly error = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly genderOptions = [
    { label: 'Nam', value: true },
    { label: 'Nữ', value: false },
  ];

  readonly registerForm = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    dob: this.fb.control<Date | null>(null, Validators.required),
    gender: this.fb.control<boolean | null>(null),
    phone: ['', Validators.required],
    address: [''],
    guardianName: [''],
    guardianPhone: [''],
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
  });

  onRegister(): void {
    this.error.set('');
    if (!this.validateForm()) {
      return;
    }

    const value = this.registerForm.getRawValue();
    const username = value.username.trim();
    const password = value.password;

    this.loading.set(true);
    this.authService
      .register({
        username,
        password,
        fullName: value.fullName.trim(),
        dob: this.toLocalDate(value.dob!),
        gender: value.gender,
        phone: this.isAdult() ? value.phone.trim() : '',
        address: value.address.trim(),
        guardianName: this.isAdult() ? '' : value.guardianName.trim(),
        guardianPhone: this.isAdult() ? '' : value.guardianPhone.trim(),
      })
      .pipe(switchMap(() => this.authService.login({ username, password })))
      .subscribe({
        next: () => {
          this.loading.set(false);
          this.closed.emit();
          this.router.navigateByUrl('/profile');
        },
        error: (err) => {
          this.loading.set(false);
          this.error.set(err.error?.message || Register.REGISTER_FAILED_MESSAGE);
        },
      });
  }

  clearFieldError(key: string): void {
    const nextErrors = { ...this.fieldErrors() };
    delete nextErrors[key];
    this.fieldErrors.set(nextErrors);
  }

  isAdult(): boolean {
    const dob = this.registerForm.controls.dob.value;
    if (!dob) {
      return true;
    }
    let age = new Date().getFullYear() - dob.getFullYear();
    const monthDiff = new Date().getMonth() - dob.getMonth();
    const dayDiff = new Date().getDate() - dob.getDate();
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
      age--;
    }
    return age >= 14;
  }

  close(): void {
    this.closed.emit();
  }

  onVisibleChange(visible: boolean): void {
    if (!visible) {
      this.close();
    }
  }

  private validateForm(): boolean {
    const value = this.registerForm.getRawValue();
    const errors: Record<string, string> = {};

    if (!value.fullName.trim()) {
      errors['fullName'] = 'Vui lòng nhập họ và tên.';
    }
    if (!value.dob) {
      errors['dob'] = 'Vui lòng chọn ngày sinh.';
    }
    if (!value.username.trim()) {
      errors['username'] = 'Vui lòng nhập tài khoản.';
    }
    if (value.password.length < 6) {
      errors['password'] = 'Mật khẩu tối thiểu 6 ký tự.';
    }
    if (value.confirmPassword !== value.password) {
      errors['confirmPassword'] = 'Mật khẩu xác nhận không khớp.';
    }
    if (this.isAdult()) {
      if (!value.phone.trim()) {
        errors['phone'] = 'Vui lòng nhập số điện thoại.';
      }
    } else {
      if (!value.guardianName.trim()) {
        errors['guardianName'] = 'Vui lòng nhập tên người giám hộ.';
      }
      if (!value.guardianPhone.trim()) {
        errors['guardianPhone'] = 'Vui lòng nhập số điện thoại người giám hộ.';
      }
    }

    this.fieldErrors.set(errors);
    return Object.keys(errors).length === 0;
  }

  private toLocalDate(value: Date): string {
    return `${value.getFullYear()}-${String(value.getMonth() + 1).padStart(2, '0')}-${String(
      value.getDate(),
    ).padStart(2, '0')}`;
  }
}
