import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';
import { Textarea } from 'primeng/textarea';
import { AppointmentResponse } from '../../core/model/response/appointment-response';
import { DentistResponse } from '../../core/model/response/dentist-response';
import { PatientProfileResponse } from '../../core/model/response/patient-profile-response';
import { SlotDetailResponse } from '../../core/model/response/slot-response';
import { ClientPortalService } from '../../core/service/client-portal.service';

@Component({
  selector: 'app-booking',
  imports: [
    CommonModule,
    FormsModule,
    RouterLink,
    Button,
    Card,
    InputText,
    Message,
    ProgressSpinner,
    Select,
    Textarea,
  ],
  templateUrl: './booking.html',
  styleUrl: './booking.css',
})
export class Booking implements OnInit {
  private readonly clientPortalService = inject(ClientPortalService);

  readonly today = this.dateInput(new Date());
  readonly tomorrow = this.dateInput(this.addDays(new Date(), 1));
  readonly consultationDurationMinutes = 30;
  readonly profile = signal<PatientProfileResponse | null>(null);
  readonly dentists = signal<DentistResponse[]>([]);
  readonly slots = signal<SlotDetailResponse[]>([]);
  readonly appointments = signal<AppointmentResponse[]>([]);
  readonly loading = signal(false);
  readonly slotLoading = signal(false);
  readonly submitting = signal(false);
  readonly message = signal('');
  readonly error = signal('');

  booking = {
    dentistId: null as number | null,
    date: this.tomorrow,
    time: '',
    symptom: '',
  };

  ngOnInit(): void {
    this.loadPageData();
  }

  loadPageData(): void {
    this.loading.set(true);
    this.loadProfile();
    this.loadAppointments();
    this.loadLookups();
  }

  loadSlots(): void {
    if (!this.booking.dentistId || !this.booking.date) {
      this.slots.set([]);
      return;
    }

    this.slotLoading.set(true);
    this.clientPortalService
      .getAvailableSlots(
        this.booking.dentistId,
        this.booking.date,
        this.consultationDurationMinutes,
      )
      .subscribe({
        next: (res) => {
          this.slots.set((res.data?.slotDetails ?? []).filter((slot) => slot.available));
          this.slotLoading.set(false);
        },
        error: () => {
          this.slots.set([]);
          this.slotLoading.set(false);
          this.error.set('Không tải được khung giờ khám.');
        },
      });
  }

  onSlotContextChange(): void {
    this.booking.time = '';
    this.loadSlots();
  }

  selectSlot(slot: SlotDetailResponse): void {
    if (slot.available) {
      this.booking.time = slot.time;
    }
  }

  bookAppointment(): void {
    this.clearNotices();
    if (!this.booking.dentistId || !this.booking.date || !this.booking.time) {
      this.error.set('Vui lòng chọn bác sĩ, ngày khám và giờ khám.');
      return;
    }
    if (this.booking.date <= this.today) {
      this.error.set('Website khách hàng chỉ cho phép đặt lịch từ ngày mai.');
      return;
    }

    this.submitting.set(true);
    this.clientPortalService
      .bookAppointment({
        dentistId: this.booking.dentistId,
        appointmentDate: `${this.booking.date}T${this.booking.time}:00`,
        durationMinutes: this.consultationDurationMinutes,
        symptom: this.booking.symptom.trim(),
      })
      .subscribe({
        next: () => {
          this.submitting.set(false);
          this.message.set('Đã gửi lịch hẹn. Phòng khám sẽ xác nhận lại với bạn.');
          this.booking.time = '';
          this.booking.symptom = '';
          this.loadSlots();
          this.loadAppointments();
        },
        error: (err) => {
          this.submitting.set(false);
          this.error.set(err.error?.message || 'Không thể đặt lịch ở khung giờ này.');
          this.loadSlots();
        },
      });
  }

  selectedDentist(): DentistResponse | null {
    return this.dentists().find((item) => item.id === this.booking.dentistId) ?? null;
  }

  dentistOptions(): { label: string; value: number }[] {
    return this.dentists().map((item) => ({
      label: item.fullName,
      value: item.id,
    }));
  }

  slotButtonClass(slot: SlotDetailResponse): string {
    return [
      'slot-button',
      this.booking.time === slot.time ? 'slot-selected' : '',
      !slot.available ? 'slot-busy' : '',
    ]
      .filter(Boolean)
      .join(' ');
  }

  upcomingAppointment(): AppointmentResponse | null {
    const now = Date.now();
    return (
      this.appointments()
        .filter((item) => !['COMPLETED', 'CANCELLED'].includes(item.status))
        .filter((item) => new Date(item.appointmentDate).getTime() >= now)
        .sort(
          (a, b) =>
            new Date(a.appointmentDate).getTime() - new Date(b.appointmentDate).getTime(),
        )[0] ?? null
    );
  }

  formatDateTime(value: string | null): string {
    if (!value) {
      return 'Chưa có';
    }
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(value));
  }

  formatMoney(value: number | null | undefined): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(value ?? 0);
  }

  private loadProfile(): void {
    this.clientPortalService.getProfile().subscribe({
      next: (res) => this.profile.set(res.data),
      error: (err) => this.error.set(err.error?.message || 'Không tải được hồ sơ cá nhân.'),
    });
  }

  private loadLookups(): void {
    this.clientPortalService.getDentists().subscribe({
      next: (res) => {
        this.dentists.set(res.data ?? []);
        if (!this.booking.dentistId && this.dentists().length) {
          this.booking.dentistId = this.dentists()[0].id;
        }
        this.loadSlots();
      },
      complete: () => this.loading.set(false),
    });
  }

  private loadAppointments(): void {
    this.clientPortalService.getAppointments().subscribe({
      next: (res) => this.appointments.set(res.data ?? []),
    });
  }

  private clearNotices(): void {
    this.message.set('');
    this.error.set('');
  }

  private dateInput(date: Date): string {
    return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
      date.getDate(),
    ).padStart(2, '0')}`;
  }

  private addDays(date: Date, days: number): Date {
    const next = new Date(date);
    next.setDate(next.getDate() + days);
    return next;
  }
}
