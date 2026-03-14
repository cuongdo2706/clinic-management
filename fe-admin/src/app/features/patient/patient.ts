import {Component, inject, OnInit, signal} from '@angular/core';
import {PatientService} from "./service/patient.service";
import {PatientRequest, PatientResponse} from "./model/patient.model";
import {MessageService, ConfirmationService} from "primeng/api";
import {Toast} from "primeng/toast";
import {ConfirmDialog} from "primeng/confirmdialog";
import {TableModule} from "primeng/table";
import {Button} from "primeng/button";
import {Dialog} from "primeng/dialog";
import {InputText} from "primeng/inputtext";
import {FloatLabel} from "primeng/floatlabel";
import {FormsModule} from "@angular/forms";
import {IconField} from "primeng/iconfield";
import {InputIcon} from "primeng/inputicon";
import {Toolbar} from "primeng/toolbar";
import {Select} from "primeng/select";
import {HttpErrorResponse} from "@angular/common/http";

@Component({
    selector: 'app-patient',
    imports: [
        Toast,
        ConfirmDialog,
        TableModule,
        Button,
        Dialog,
        InputText,
        FloatLabel,
        FormsModule,
        IconField,
        InputIcon,
        Toolbar,
        Select,
    ],
    providers: [MessageService, ConfirmationService],
    templateUrl: './patient.html',
    styleUrl: './patient.css',
})
export class Patient implements OnInit {
    private readonly patientService = inject(PatientService);
    private readonly messageService = inject(MessageService);
    private readonly confirmationService = inject(ConfirmationService);

    patients = signal<PatientResponse[]>([]);
    loading = signal(false);
    dialogVisible = signal(false);
    isEdit = signal(false);
    searchKeyword = signal('');

    genderOptions = [
        {label: 'Nam', value: 'MALE'},
        {label: 'Nữ', value: 'FEMALE'},
    ];

    selectedId = '';
    formData: PatientRequest = {
        fullName: '', phone: '', email: '', dateOfBirth: '', gender: '', address: ''
    };

    ngOnInit() {
        this.loadData();
    }

    loadData() {
        this.loading.set(true);
        this.patientService.getAll(0, 100, this.searchKeyword()).subscribe({
            next: (res) => {
                this.patients.set(res.data?.content ?? res.data ?? []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false),
        });
    }

    onSearch() {
        this.loadData();
    }

    openNew() {
        this.formData = {fullName: '', phone: '', email: '', dateOfBirth: '', gender: '', address: ''};
        this.isEdit.set(false);
        this.dialogVisible.set(true);
    }

    openEdit(patient: PatientResponse) {
        this.selectedId = patient.id;
        this.formData = {
            fullName: patient.fullName,
            phone: patient.phone,
            email: patient.email,
            dateOfBirth: patient.dateOfBirth,
            gender: patient.gender,
            address: patient.address,
        };
        this.isEdit.set(true);
        this.dialogVisible.set(true);
    }

    getGenderLabel(gender: string): string {
        return gender === 'MALE' ? 'Nam' : gender === 'FEMALE' ? 'Nữ' : gender;
    }

    save() {
        if (this.isEdit()) {
            this.patientService.update(this.selectedId, this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Cập nhật bệnh nhân thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        } else {
            this.patientService.create(this.formData).subscribe({
                next: () => {
                    this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Thêm bệnh nhân thành công'});
                    this.dialogVisible.set(false);
                    this.loadData();
                },
                error: (err: HttpErrorResponse) => {
                    this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Có lỗi xảy ra'});
                },
            });
        }
    }

    confirmDelete(patient: PatientResponse) {
        this.confirmationService.confirm({
            message: `Bạn có chắc muốn xoá bệnh nhân <b>${patient.fullName}</b>?`,
            header: 'Xác nhận xoá',
            icon: 'pi pi-exclamation-triangle',
            acceptLabel: 'Xoá',
            rejectLabel: 'Huỷ',
            accept: () => {
                this.patientService.delete(patient.id).subscribe({
                    next: () => {
                        this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã xoá bệnh nhân'});
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



