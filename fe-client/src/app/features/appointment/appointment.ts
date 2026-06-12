import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { InputText } from 'primeng/inputtext';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Select } from 'primeng/select';
import { Tag } from 'primeng/tag';
import {
  AppointmentArrivalStatus,
  AppointmentResponse,
  AppointmentStatus,
} from '../../core/model/response/appointment-response';
import { ClientPortalService } from '../../core/service/client-portal.service';

@Component({
  selector: 'app-appointment',
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
    Tag,
  ],
  templateUrl: './appointment.html',
  styleUrl: './appointment.css',
})
export class Appointment implements OnInit {
  private readonly clientPortalService = inject(ClientPortalService);

  readonly appointments = signal<AppointmentResponse[]>([]);
  readonly loading = signal(false);
  readonly cancellingId = signal<number | null>(null);
  readonly error = signal('');
  readonly message = signal('');
  readonly statusFilterOptions = [
    { label: 'Tất cả trạng thái', value: 'ALL' },
    { label: 'Chờ xác nhận', value: 'PENDING' },
    { label: 'Đã xác nhận', value: 'CONFIRMED' },
    { label: 'Đang khám', value: 'IN_PROGRESS' },
    { label: 'Hoàn thành', value: 'COMPLETED' },
    { label: 'Đã hủy', value: 'CANCELLED' },
  ];
  readonly statusFilter = signal<AppointmentStatus | 'ALL'>('ALL');
  readonly keyword = signal('');
  readonly filteredAppointments = computed(() => {
    const keyword = this.keyword().trim().toLowerCase();
    const status = this.statusFilter();
    return this.appointments().filter((item) => {
      const matchesStatus = status === 'ALL' || item.status === status;
      const matchesKeyword =
        !keyword ||
        [item.code, item.dentistName, item.symptom, item.note]
          .filter(Boolean)
          .some((value) => String(value).toLowerCase().includes(keyword));
      return matchesStatus && matchesKeyword;
    });
  });

  ngOnInit(): void {
    this.loadData();
  }

  loadData(): void {
    this.loading.set(true);
    this.error.set('');
    this.message.set('');
    this.clientPortalService.getAppointments().subscribe({
      next: (res) => {
        this.appointments.set(res.data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Không tải được danh sách lịch khám.');
      },
    });
  }

  cancelAppointment(item: AppointmentResponse): void {
    this.error.set('');
    this.message.set('');
    if (!this.canCancel(item)) {
      this.error.set('Chỉ có thể hủy lịch đang chờ xác nhận hoặc đã xác nhận.');
      return;
    }
    if (!window.confirm(`Hủy lịch khám ${item.code}?`)) {
      return;
    }

    this.cancellingId.set(item.id);
    this.clientPortalService.cancelAppointment(item.id).subscribe({
      next: (res) => {
        const updated = res.data;
        this.appointments.update((items) =>
          items.map((current) => (current.id === updated.id ? updated : current)),
        );
        this.cancellingId.set(null);
        this.message.set('Đã hủy lịch khám.');
      },
      error: (err) => {
        this.cancellingId.set(null);
        this.error.set(err.error?.message || 'Không thể hủy lịch khám này.');
      },
    });
  }

  canCancel(item: AppointmentResponse): boolean {
    return (
      ['PENDING', 'CONFIRMED'].includes(item.status) &&
      (!item.arrivalStatus || item.arrivalStatus === 'NOT_ARRIVED')
    );
  }

  statusLabel(status: AppointmentStatus): string {
    const labels: Record<AppointmentStatus, string> = {
      PENDING: 'Chờ xác nhận',
      CONFIRMED: 'Đã xác nhận',
      IN_PROGRESS: 'Đang khám',
      COMPLETED: 'Hoàn thành',
      CANCELLED: 'Đã hủy',
    };
    return labels[status] ?? status;
  }

  arrivalStatusLabel(status: AppointmentArrivalStatus | null): string {
    const labels: Record<AppointmentArrivalStatus, string> = {
      NOT_ARRIVED: 'Chưa đến',
      ARRIVED: 'Đã đến',
      NO_SHOW: 'Không đến',
    };
    return status ? labels[status] : 'Chưa đến';
  }

  statusSeverity(status: AppointmentStatus): 'success' | 'secondary' | 'info' | 'warn' | 'danger' {
    if (status === 'COMPLETED') {
      return 'success';
    }
    if (status === 'CANCELLED') {
      return 'danger';
    }
    if (status === 'IN_PROGRESS') {
      return 'warn';
    }
    if (status === 'PENDING') {
      return 'info';
    }
    return 'secondary';
  }

  arrivalSeverity(status: AppointmentArrivalStatus | null): 'success' | 'secondary' | 'info' | 'warn' | 'danger' {
    if (status === 'ARRIVED') {
      return 'success';
    }
    if (status === 'NO_SHOW') {
      return 'danger';
    }
    return 'secondary';
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

  formatDate(value: string | null): string {
    if (!value) {
      return 'Chưa có';
    }
    return new Intl.DateTimeFormat('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
    }).format(new Date(value));
  }
}
