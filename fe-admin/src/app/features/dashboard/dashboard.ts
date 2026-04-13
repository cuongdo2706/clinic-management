import {afterNextRender, Component, ElementRef, inject, OnDestroy, signal, viewChild} from '@angular/core';
import {DashboardService, RecentAppointment} from "../../core/service/dashboard.service";
import {Tag} from "primeng/tag";
import {TableModule} from "primeng/table";
import {Chart, registerables} from 'chart.js';

Chart.register(...registerables);

interface StatCard {
    label: string;
    value: number;
    icon: string;
    color: string;
    bg: string;
}

@Component({
    selector: 'app-dashboard',
    imports: [
        Tag,
        TableModule,
    ],
    templateUrl: './dashboard.html',
    styleUrl: './dashboard.css',
})
export class Dashboard implements OnDestroy {
    private readonly dashboardService = inject(DashboardService);

    private barChart: Chart | null = null;
    private pieChart: Chart | null = null;

    barCanvas = viewChild<ElementRef<HTMLCanvasElement>>('barCanvas');
    pieCanvas = viewChild<ElementRef<HTMLCanvasElement>>('pieCanvas');

    loading = signal(true);

    statCards = signal<StatCard[]>([
        {label: 'Nha sĩ', value: 0, icon: 'pi pi-users', color: '#16a34a', bg: '#dcfce7'},
        {label: 'Bệnh nhân', value: 0, icon: 'pi pi-user', color: '#0891b2', bg: '#cffafe'},
        {label: 'Lịch hẹn hôm nay', value: 0, icon: 'pi pi-calendar', color: '#d97706', bg: '#fef3c7'},
        {label: 'Dịch vụ', value: 0, icon: 'pi pi-list', color: '#7c3aed', bg: '#ede9fe'},
    ]);

    recentAppointments = signal<RecentAppointment[]>([]);

    constructor() {
        afterNextRender(() => this.loadData());
    }

    ngOnDestroy() {
        this.barChart?.destroy();
        this.pieChart?.destroy();
    }

    loadData() {
        this.loading.set(true);
        this.dashboardService.getStats().subscribe({
            next: (res) => {
                const stats = res.data;
                if (stats) {
                    this.statCards.set([
                        {label: 'Nha sĩ', value: stats.totalDentists, icon: 'pi pi-users', color: '#16a34a', bg: '#dcfce7'},
                        {label: 'Bệnh nhân', value: stats.totalPatients, icon: 'pi pi-user', color: '#0891b2', bg: '#cffafe'},
                        {label: 'Lịch hẹn hôm nay', value: stats.totalAppointmentsToday, icon: 'pi pi-calendar', color: '#d97706', bg: '#fef3c7'},
                        {label: 'Dịch vụ', value: stats.totalServices, icon: 'pi pi-list', color: '#7c3aed', bg: '#ede9fe'},
                    ]);
                    this.recentAppointments.set(stats.recentAppointments ?? []);
                    this.renderBarChart(stats.appointmentsByMonth ?? []);
                    this.renderPieChart(stats.serviceUsage ?? []);
                }
                this.loading.set(false);
            },
            error: () => {
                this.setDemoData();
                this.loading.set(false);
            },
        });
    }

    /** Demo data khi API chưa có */
    private setDemoData() {
        this.statCards.set([
            {label: 'Nha sĩ', value: 12, icon: 'pi pi-users', color: '#16a34a', bg: '#dcfce7'},
            {label: 'Bệnh nhân', value: 248, icon: 'pi pi-user', color: '#0891b2', bg: '#cffafe'},
            {label: 'Lịch hẹn hôm nay', value: 8, icon: 'pi pi-calendar', color: '#d97706', bg: '#fef3c7'},
            {label: 'Dịch vụ', value: 15, icon: 'pi pi-list', color: '#7c3aed', bg: '#ede9fe'},
        ]);
        this.recentAppointments.set([
            {id: '1', patientName: 'Nguyễn Văn A', dentistName: 'BS. Trần B', appointmentDate: '2026-03-08', timeSlot: '08:00 - 09:00', status: 'CONFIRMED'},
            {id: '2', patientName: 'Lê Thị C', dentistName: 'BS. Phạm D', appointmentDate: '2026-03-08', timeSlot: '09:00 - 10:00', status: 'PENDING'},
            {id: '3', patientName: 'Hoàng Văn E', dentistName: 'BS. Trần B', appointmentDate: '2026-03-08', timeSlot: '10:00 - 11:00', status: 'COMPLETED'},
            {id: '4', patientName: 'Trần Thị F', dentistName: 'BS. Nguyễn G', appointmentDate: '2026-03-08', timeSlot: '13:00 - 14:00', status: 'CANCELLED'},
            {id: '5', patientName: 'Võ Minh H', dentistName: 'BS. Phạm D', appointmentDate: '2026-03-08', timeSlot: '14:00 - 15:00', status: 'CONFIRMED'},
        ]);
        this.renderBarChart([
            {month: 'T1', count: 45}, {month: 'T2', count: 52}, {month: 'T3', count: 61},
            {month: 'T4', count: 48}, {month: 'T5', count: 55}, {month: 'T6', count: 70},
            {month: 'T7', count: 63}, {month: 'T8', count: 58}, {month: 'T9', count: 72},
            {month: 'T10', count: 80}, {month: 'T11', count: 67}, {month: 'T12', count: 75},
        ]);
        this.renderPieChart([
            {serviceName: 'Nhổ răng', count: 35},
            {serviceName: 'Trám răng', count: 50},
            {serviceName: 'Tẩy trắng', count: 25},
            {serviceName: 'Niềng răng', count: 18},
            {serviceName: 'Bọc sứ', count: 30},
        ]);
    }

    private renderBarChart(data: { month: string; count: number }[]) {
        const canvas = this.barCanvas()?.nativeElement;
        if (!canvas) return;
        this.barChart?.destroy();
        this.barChart = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: data.map(d => d.month),
                datasets: [{
                    label: 'Lịch hẹn',
                    data: data.map(d => d.count),
                    backgroundColor: '#22c55e',
                    borderColor: '#16a34a',
                    borderWidth: 1,
                    borderRadius: 6,
                }],
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {legend: {display: false}},
                scales: {
                    y: {beginAtZero: true, ticks: {stepSize: 10}, grid: {color: '#e5e7eb'}},
                    x: {grid: {display: false}},
                },
            },
        });
    }

    private renderPieChart(data: { serviceName: string; count: number }[]) {
        const canvas = this.pieCanvas()?.nativeElement;
        if (!canvas) return;
        this.pieChart?.destroy();
        const colors = ['#22c55e', '#0891b2', '#d97706', '#7c3aed', '#e11d48', '#0d9488', '#ea580c'];
        this.pieChart = new Chart(canvas, {
            type: 'doughnut',
            data: {
                labels: data.map(d => d.serviceName),
                datasets: [{
                    data: data.map(d => d.count),
                    backgroundColor: data.map((_, i) => colors[i % colors.length]),
                    hoverBackgroundColor: data.map((_, i) => colors[i % colors.length] + 'cc'),
                }],
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {position: 'bottom', labels: {padding: 16, usePointStyle: true}},
                },
            },
        });
    }

    getStatusLabel(status: string): string {
        switch (status?.toUpperCase()) {
            case 'CONFIRMED': return 'Đã xác nhận';
            case 'PENDING': return 'Chờ xác nhận';
            case 'COMPLETED': return 'Hoàn thành';
            case 'CANCELLED': return 'Đã huỷ';
            default: return status;
        }
    }

    getStatusSeverity(status: string): "success" | "info" | "warn" | "danger" | "secondary" {
        switch (status?.toUpperCase()) {
            case 'CONFIRMED': return 'success';
            case 'PENDING': return 'warn';
            case 'COMPLETED': return 'info';
            case 'CANCELLED': return 'danger';
            default: return 'secondary';
        }
    }
}
