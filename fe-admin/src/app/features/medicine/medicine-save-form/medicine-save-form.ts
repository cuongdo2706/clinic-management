import {Component, inject, output, signal} from '@angular/core';
import {MedicineService} from "../../../core/service/medicine.service";
import {form, FormField} from "@angular/forms/signals";
import {CreateMedicineRequest} from "../../../core/model/request/create-medicine-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";

interface MedicineFormData {
    code: string;
    name: string;
    unit: string;
    price: string;
    quantity: string;
    manufacturer: string;
    origin: string;
    description: string;
}

const EMPTY_FORM: MedicineFormData = {
    code: '',
    name: '',
    unit: '',
    price: '',
    quantity: '',
    manufacturer: '',
    origin: '',
    description: '',
};

@Component({
    selector: 'app-medicine-save-form',
    imports: [Button, InputText, FormField, Card],
    templateUrl: './medicine-save-form.html',
    styleUrl: './medicine-save-form.css',
})
export class MedicineSaveForm {
    private readonly medicineService = inject(MedicineService);
    private readonly messageService = inject(MessageService);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    saveForm = form(signal<MedicineFormData>(EMPTY_FORM));

    private validate(): boolean {
        const val = this.saveForm().value();
        const errs: Record<string, string> = {};
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên thuốc';
        if (!val.unit?.trim()) errs['unit'] = 'Vui lòng nhập đơn vị';
        if (!val.price || isNaN(Number(val.price)) || Number(val.price) < 0) errs['price'] = 'Vui lòng nhập giá hợp lệ';
        if (!val.quantity || isNaN(Number(val.quantity)) || Number(val.quantity) < 0) errs['quantity'] = 'Vui lòng nhập số lượng hợp lệ';
        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    onSubmit(): void {
        if (!this.validate()) return;
        const val = this.saveForm().value();
        const request: CreateMedicineRequest = {
            code: val.code,
            name: val.name,
            unit: val.unit,
            price: Number(val.price),
            quantity: Number(val.quantity),
            manufacturer: val.manufacturer,
            origin: val.origin,
            description: val.description,
        };
        this.loading.set(true);
        this.medicineService.create(request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Đã thêm thuốc thành công'
                });
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể thêm thuốc, vui lòng thử lại'
                });
            }
        });
    }
}
