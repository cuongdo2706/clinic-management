import {Component, inject, input, OnInit, output, signal} from '@angular/core';
import {MedicineService} from "../../../core/service/medicine.service";
import {form, FormField} from "@angular/forms/signals";
import {UpdateMedicineRequest} from "../../../core/model/request/update-medicine-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {ProgressSpinner} from "primeng/progressspinner";

interface MedicineFormData {
    code: string;
    name: string;
    unit: string;
    description: string;
}

@Component({
    selector: 'app-medicine-update-form',
    imports: [Button, InputText, FormField, Card, ProgressSpinner],
    templateUrl: './medicine-update-form.html',
    styleUrl: './medicine-update-form.css',
})
export class MedicineUpdateForm implements OnInit {
    private readonly medicineService = inject(MedicineService);
    private readonly messageService = inject(MessageService);

    medicine = input.required<number>();

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);
    errors = signal<Record<string, string>>({});
    private medicineVersion = 0;

    updateForm = form(signal<MedicineFormData>({
        code: '',
        name: '',
        unit: '',
        description: '',
    }));

    ngOnInit(): void {
        this.medicineService.findById(this.medicine().toString()).subscribe({
            next: res => {
                const m = res.data;
                this.medicineVersion = m.version;
                this.updateForm().reset({
                    code: m.code,
                    name: m.name,
                    unit: m.unit,
                    description: m.description || '',
                });
                this.fetching.set(false);
            },
            error: () => {
                this.fetching.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải thông tin thuốc'
                });
                this.cancelled.emit();
            }
        });
    }

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    private validate(): boolean {
        const val = this.updateForm().value();
        const errs: Record<string, string> = {};
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên thuốc';
        if (!val.unit?.trim()) errs['unit'] = 'Vui lòng nhập đơn vị';
        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;

        const val = this.updateForm().value();
        const request: UpdateMedicineRequest = {
            code: val.code,
            name: val.name,
            unit: val.unit,
            description: val.description,
        };
        this.loading.set(true);
        this.medicineService.update(this.medicine().toString(), request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Đã cập nhật thuốc thành công'
                });
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể cập nhật thuốc, vui lòng thử lại'
                });
            }
        });
    }
}
