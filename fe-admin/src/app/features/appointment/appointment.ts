import {Component, inject, OnInit, signal} from '@angular/core';
import {AppointmentService} from "./service/appointment.service";
import {AppointmentRequest, AppointmentResponse} from "./model/appointment.model";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {Dialog} from "primeng/dialog";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormsModule} from "@angular/forms";
import {Tag} from "primeng/tag";
import {IconField} from "primeng/iconfield";
import {InputIcon} from "primeng/inputicon";
import {Toolbar} from "primeng/toolbar";
import {DatePicker} from "primeng/datepicker";
import {Textarea} from "primeng/textarea";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
    selector: 'app-appointment',
    imports: [
        Toast,
        ConfirmDialog,
        TableModule,
        Button,
        Dialog,
        InputText,
        FloatLabel,
        FormsModule,
        Tag,
        IconField,
        InputIcon,
        Toolbar,
        Textarea,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './appointment.html',
    styleUrl: './appointment.css',
})
export class Appointment implements OnInit {
    private readonly appointmentService = inject(AppointmentService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    appointments = signal<AppointmentResponse[]>([]);
    loading = signal(false);
    dialogVisible = signal(false);
    isEdit = signal(false);
    searchKeyword = signal('');

    selectedId = '';
    formData: AppointmentRequest = {
        patientId: '', dentistId: '', appointmentDate: '', timeSlot: '', notes: ''
    };

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.appointmentService.getAll(0, 100, this.searchKeyword()).subscribe({
            next: (res) => {
                this.appointments.set(res.data?.content ?? res.data ?? []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false),
        });
    }

    onSearch() {
        this.loadData();
    }

    openNew() {
        this.formData = {patientId: '', dentistId: '', appointmentDate: '', timeSlot: '', notes: ''};
        this.isEdit.set(false);
        this.dialogVisible.set(true);
    }

    openEdit(appointment: AppointmentResponse) {
        this.selectedId = appointment.id;
        this.formData = {
            patientId: '',
            dentistId: '',
            appointmentDate: appointment.appointmentDate,
            timeSlot: appointment.timeSlot,
            notes: appointment.notes,
        };
        this.isEdit.set(true);
        this.dialogVisible.set(true);
    }

    getStatusSeverity(status: string): "success" | "info" | "warn" | "danger" | "secondary" | "contrast" {
        switch (status?.toUpperCase()) {
            case 'CONFIRMED': return 'success';
            case 'PENDING': return 'warn';
            case 'CANCELLED': return 'danger';
            case 'COMPLETED': return 'info';
            default: return 'secondary';
        }
    }

    save() {
        if (this.isEdit()) {
            this.appointmentService.update(this.selectedId, this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Cập nhật lịch hẹn thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        } else {
            this.appointmentService.create(this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Thêm lịch hẹn thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        }
    }

    confirmDelete(appointment: AppointmentResponse) {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xoá lịch hẹn của <b>${appointment.patientName}</b>?`,
            header: 'Xác nhận xoá',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xoá',
            rejectLabel: 'Huỷ',
            accept: () => {
                this.appointmentService.delete(appointment.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xoá lịch hẹn'});
                        this.loadData();
                    },
                    error: (err: HttpErrorResponse) => {
                        this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                    },
                });
            },
        });
    }
}




