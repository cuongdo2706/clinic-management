import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { DatePicker } from 'primeng/datepicker';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { PatientProfileResponse } from '../../core/model/response/patient-profile-response';
import { ClientPortalService } from '../../core/service/client-portal.service';

@Component({
  selector: 'app-profile',
  imports: [
    ReactiveFormsModule,
    Button,
    Card,
    DatePicker,
    InputText,
    Message,
    Select,
    Textarea,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private readonly clientPortalService = inject(ClientPortalService);
  private readonly fb = inject(FormBuilder);

  readonly profile = signal<PatientProfileResponse | null>(null);
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly message = signal('');
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly genderOptions = [
    { label: 'Nam', value: true },
    { label: 'Nữ', value: false },
  ];

  readonly profileForm = this.fb.group({
    code: this.fb.nonNullable.control({ value: '', disabled: true }),
    fullName: this.fb.nonNullable.control(''),
    dob: this.fb.control<Date | null>(null),
    gender: this.fb.control<boolean | null>(null),
    phone: this.fb.nonNullable.control(''),
    address: this.fb.nonNullable.control(''),
    guardianName: this.fb.nonNullable.control(''),
    guardianPhone: this.fb.nonNullable.control(''),
  });

  readonly isAdult = computed(() => {
    const dob = this.profileForm.controls.dob.value;
    if (!dob) return true;
    let age = new Date().getFullYear() - dob.getFullYear();
    const monthDiff = new Date().getMonth() - dob.getMonth();
    const dayDiff = new Date().getDate() - dob.getDate();
    if (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)) {
      age--;
    }
    return age > 14;
  });

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.loading.set(true);
    this.error.set('');
    this.message.set('');
    this.clientPortalService.getProfile().subscribe({
      next: (res) => {
        this.profile.set(res.data);
        this.patchForm(res.data);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Không tải được thông tin cá nhân.');
      },
    });
  }

  updateProfile(): void {
    this.error.set('');
    this.message.set('');
    if (!this.validateForm()) {
      return;
    }

    const currentProfile = this.profile();
    const value = this.profileForm.getRawValue();
    if (!currentProfile || !value.dob || value.gender === null) {
      return;
    }

    this.saving.set(true);
    this.clientPortalService
      .updateProfile({
        code: currentProfile.code,
        fullName: value.fullName.trim(),
        dob: this.toLocalDate(value.dob),
        gender: value.gender,
        phone: this.isAdult() ? value.phone.trim() : '',
        address: value.address.trim(),
        guardianName: this.isAdult() ? '' : value.guardianName.trim(),
        guardianPhone: this.isAdult() ? '' : value.guardianPhone.trim(),
        version: currentProfile.version,
      })
      .subscribe({
        next: (res) => {
          this.profile.set(res.data);
          this.patchForm(res.data);
          this.saving.set(false);
          this.message.set('Đã cập nhật thông tin cá nhân.');
        },
        error: (err) => {
          this.saving.set(false);
          this.error.set(err.error?.message || 'Không thể cập nhật thông tin cá nhân.');
        },
      });
  }

  clearFieldError(key: string): void {
    const nextErrors = { ...this.fieldErrors() };
    delete nextErrors[key];
    this.fieldErrors.set(nextErrors);
  }

  genderLabel(value: boolean | null): string {
    if (value === true) return 'Nam';
    if (value === false) return 'Nữ';
    return 'Chưa cập nhật';
  }

  formatDate(value: string | null): string {
    if (!value) return 'Chưa cập nhật';
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(value));
  }

  private patchForm(profile: PatientProfileResponse): void {
    this.profileForm.reset({
      code: profile.code,
      fullName: profile.fullName,
      dob: profile.dob ? new Date(profile.dob) : null,
      gender: profile.gender,
      phone: profile.phone ?? '',
      address: profile.address ?? '',
      guardianName: profile.guardianName ?? '',
      guardianPhone: profile.guardianPhone ?? '',
    });
    this.fieldErrors.set({});
  }

  private validateForm(): boolean {
    const value = this.profileForm.getRawValue();
    const errors: Record<string, string> = {};

    if (!value.fullName.trim()) {
      errors['fullName'] = 'Vui lòng nhập họ và tên.';
    }
    if (!value.dob) {
      errors['dob'] = 'Vui lòng chọn ngày sinh.';
    }
    if (value.gender === null) {
      errors['gender'] = 'Vui lòng chọn giới tính.';
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
