import {afterNextRender, Component, computed, ElementRef, inject, OnDestroy, signal, viewChild} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {
    DailyAppointment,
    DashboardService,
    HourlyAppointment,
    MonthlyAppointment,
    RecentAppointment
} from "../../core/service/dashboard.service";
import {Tag} from "primeng/tag";
import {TableModule} from "primeng/table";
import {Select} from "primeng/select";
import {Chart, registerables} from 'chart.js';

Chart.register(...registerables);

interface StatCard {
    label: string;
    value: number;
    icon: string;
    color: string;
    bg: string;
}

type AppointmentChartMode = 'MONTH' | 'DAY' | 'HOUR';

interface AppointmentChartPoint {
    label: string;
    count: number;
}

@Component({
    selector: 'app-dashboard',
    imports: [
        FormsModule,
        Select,
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
    error = signal('');
    appointmentChartMode = signal<AppointmentChartMode>('MONTH');
    appointmentChartModeOptions = [
        {label: 'Theo tháng', value: 'MONTH'},
        {label: 'Theo ngày', value: 'DAY'},
        {label: 'Theo giờ', value: 'HOUR'},
    ];
    appointmentChartTitle = computed(() => {
        switch (this.appointmentChartMode()) {
            case 'DAY':
                return 'Lịch hẹn theo ngày';
            case 'HOUR':
                return 'Lịch hẹn theo giờ';
            default:
                return 'Lịch hẹn theo tháng';
        }
    });

    statCards = signal<StatCard[]>([
        {label: 'Lịch hẹn hôm nay', value: 0, icon: 'pi pi-calendar', color: '#d97706', bg: '#fef3c7'},
        {label: 'Chờ xác nhận', value: 0, icon: 'pi pi-clock', color: '#7c3aed', bg: '#ede9fe'},
        {label: 'Bệnh nhân đã đến', value: 0, icon: 'pi pi-user-check', color: '#0891b2', bg: '#cffafe'},
        {label: 'Hoàn thành hôm nay', value: 0, icon: 'pi pi-check-circle', color: '#16a34a', bg: '#dcfce7'},
    ]);

    recentAppointments = signal<RecentAppointment[]>([]);
    appointmentsByMonth = signal<MonthlyAppointment[]>([]);
    appointmentsByDay = signal<DailyAppointment[]>([]);
    appointmentsByHour = signal<HourlyAppointment[]>([]);

    constructor() {
        afterNextRender(() => this.loadData());
    }

    ngOnDestroy() {
        this.barChart?.destroy();
        this.pieChart?.destroy();
    }

    loadData() {
        this.loading.set(true);
        this.error.set('');
        this.dashboardService.getStats().subscribe({
            next: (res) => {
                const stats = res.data;
                if (stats) {
                    this.statCards.set([
                        {label: 'Lịch hẹn hôm nay', value: stats.totalAppointmentsToday, icon: 'pi pi-calendar', color: '#d97706', bg: '#fef3c7'},
                        {label: 'Chờ xác nhận', value: stats.pendingAppointmentsToday, icon: 'pi pi-clock', color: '#7c3aed', bg: '#ede9fe'},
                        {label: 'Bệnh nhân đã đến', value: stats.arrivedPatientsToday, icon: 'pi pi-user-check', color: '#0891b2', bg: '#cffafe'},
                        {label: 'Hoàn thành hôm nay', value: stats.completedAppointmentsToday, icon: 'pi pi-check-circle', color: '#16a34a', bg: '#dcfce7'},
                    ]);
                    this.recentAppointments.set(stats.recentAppointments ?? []);
                    this.appointmentsByMonth.set(stats.appointmentsByMonth ?? []);
                    this.appointmentsByDay.set(stats.appointmentsByDay ?? []);
                    this.appointmentsByHour.set(stats.appointmentsByHour ?? []);
                    this.renderAppointmentChart();
                    this.renderPieChart(stats.serviceUsage ?? []);
                }
                this.loading.set(false);
            },
            error: () => {
                this.error.set('Không tải được dữ liệu tổng quan');
                this.recentAppointments.set([]);
                this.appointmentsByMonth.set([]);
                this.appointmentsByDay.set([]);
                this.appointmentsByHour.set([]);
                this.renderAppointmentChart();
                this.renderPieChart([]);
                this.loading.set(false);
            },
        });
    }

    onAppointmentChartModeChange(mode: AppointmentChartMode): void {
        this.appointmentChartMode.set(mode);
        this.renderAppointmentChart();
    }

    private renderAppointmentChart(): void {
        const mode = this.appointmentChartMode();
        const data: AppointmentChartPoint[] = mode === 'MONTH'
            ? this.appointmentsByMonth().map(item => ({label: item.month, count: item.count}))
            : mode === 'DAY'
                ? this.appointmentsByDay().map(item => ({label: item.day, count: item.count}))
                : this.appointmentsByHour().map(item => ({label: item.hour, count: item.count}));
        this.renderBarChart(data);
    }

    private renderBarChart(data: AppointmentChartPoint[]) {
        const canvas = this.barCanvas()?.nativeElement;
        if (!canvas) return;
        this.barChart?.destroy();
        this.barChart = new Chart(canvas, {
            type: 'bar',
            data: {
                labels: data.map(d => d.label),
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
            case 'PENDING': return 'Chờ xác nhận';
            case 'CONFIRMED': return 'Đã xác nhận';
            case 'IN_PROGRESS': return 'Đang khám';
            case 'COMPLETED': return 'Hoàn thành';
            case 'CANCELLED': return 'Đã hủy';
            default: return status;
        }
    }

    getStatusSeverity(status: string): "success" | "info" | "warn" | "danger" | "secondary" {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'warn';
            case 'CONFIRMED': return 'success';
            case 'IN_PROGRESS': return 'info';
            case 'COMPLETED': return 'info';
            case 'CANCELLED': return 'danger';
            default: return 'secondary';
        }
    }
}
