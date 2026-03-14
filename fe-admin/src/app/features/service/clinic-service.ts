import {Component, inject, OnInit, signal} from '@angular/core';
import {ClinicServiceService} from "./service/clinic-service.service";
import {ClinicServiceRequest, ClinicServiceResponse} from "./model/clinic-service.model";
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
import {InputNumber} from "primeng/inputnumber";
import {Textarea} from "primeng/textarea";
import {HttpErrorResponse} from "@angular/common/http";
import {CurrencyPipe} from "@angular/common";

@Component({
    selector: 'app-clinic-service',
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
        InputNumber,
        Textarea,
        CurrencyPipe,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './clinic-service.html',
    styleUrl: './clinic-service.css',
})
export class ClinicService implements OnInit {
    private readonly clinicServiceService = inject(ClinicServiceService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    services = signal<ClinicServiceResponse[]>([]);
    loading = signal(false);
    dialogVisible = signal(false);
    isEdit = signal(false);
    searchKeyword = signal('');

    selectedId = '';
    formData: ClinicServiceRequest = {name: '', description: '', price: 0, duration: 30};

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.clinicServiceService.getAll(0, 100, this.searchKeyword()).subscribe({
            next: (res) => {
                this.services.set(res.data?.content ?? res.data ?? []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false),
        });
    }

    onSearch() {
        this.loadData();
    }

    openNew() {
        this.formData = {name: '', description: '', price: 0, duration: 30};
        this.isEdit.set(false);
        this.dialogVisible.set(true);
    }

    openEdit(service: ClinicServiceResponse) {
        this.selectedId = service.id;
        this.formData = {
            name: service.name,
            description: service.description,
            price: service.price,
            duration: service.duration,
        };
        this.isEdit.set(true);
        this.dialogVisible.set(true);
    }

    save() {
        if (this.isEdit()) {
            this.clinicServiceService.update(this.selectedId, this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Cập nhật dịch vụ thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        } else {
            this.clinicServiceService.create(this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Thêm dịch vụ thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        }
    }

    confirmDelete(service: ClinicServiceResponse) {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xoá dịch vụ <b>${service.name}</b>?`,
            header: 'Xác nhận xoá',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xoá',
            rejectLabel: 'Huỷ',
            accept: () => {
                this.clinicServiceService.delete(service.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xoá dịch vụ'});
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



