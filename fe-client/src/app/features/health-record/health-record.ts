import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Button } from 'primeng/button';
import { Card } from 'primeng/card';
import { Message } from 'primeng/message';
import { ProgressSpinner } from 'primeng/progressspinner';
import { Tag } from 'primeng/tag';
import { HealthRecordResponse } from '../../core/model/response/health-record-response';
import { ClientPortalService } from '../../core/service/client-portal.service';

@Component({
  selector: 'app-health-record',
  imports: [RouterLink, Button, Card, Message, ProgressSpinner, Tag],
  templateUrl: './health-record.html',
  styleUrl: './health-record.css',
})
export class HealthRecord implements OnInit {
  private readonly clientPortalService = inject(ClientPortalService);

  readonly records = signal<HealthRecordResponse[]>([]);
  readonly loading = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading.set(true);
    this.error.set('');
    this.clientPortalService.getHealthRecords().subscribe({
      next: (res) => {
        this.records.set(res.data ?? []);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.error.set(err.error?.message || 'Không tải được hồ sơ sức khỏe.');
      },
    });
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

  formatMoney(value: number | null | undefined): string {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(value ?? 0);
  }

  printPage(): void {
    window.print();
  }
}
