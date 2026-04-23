import {Component, inject, OnInit, output, signal} from '@angular/core';
import {TreatmentService} from "../../../core/service/treatment.service";
import {TreatmentCategoryService} from "../../../core/service/treatment-category.service";
import {form, FormField} from "@angular/forms/signals";
import {CreateTreatmentRequest} from "../../../core/model/request/create-treatment-request";
import {TreatmentCategoryResponse} from "../../../core/model/response/treatment-category-response";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {FormsModule} from "@angular/forms";

interface TreatmentFormData {
    code: string;
    name: string;
    description: string;
    price: string;
    unit: string;
    isActive: string;
    treatmentCategoryId: string;
}

const EMPTY_FORM: TreatmentFormData = {
    code: '',
    name: '',
    description: '',
    price: '',
    unit: '',
    isActive: 'true',
    treatmentCategoryId: '',
};

@Component({
    selector: 'app-treatment-save-form',
    imports: [Button, InputText, FormField, Card, FormsModule],
    templateUrl: './treatment-save-form.html',
    styleUrl: './treatment-save-form.css',
})
export class TreatmentSaveForm implements OnInit {
    private readonly treatmentService = inject(TreatmentService);
    private readonly treatmentCategoryService = inject(TreatmentCategoryService);
    private readonly messageService = inject(MessageService);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    categories = signal<TreatmentCategoryResponse[]>([]);

    saveForm = form(signal<TreatmentFormData>(EMPTY_FORM));

    ngOnInit(): void {
        this.treatmentCategoryService.search({page: 0, size: 200}).subscribe({
            next: res => this.categories.set(res.data.content),
        });
    }

    private validate(): boolean {
        const val = this.saveForm().value();
        const errs: Record<string, string> = {};
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên dịch vụ';
        if (!val.price || isNaN(Number(val.price)) || Number(val.price) < 0)
            errs['price'] = 'Vui lòng nhập giá hợp lệ';
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
        const request: CreateTreatmentRequest = {
            code: val.code,
            name: val.name,
            description: val.description,
            price: Number(val.price),
            unit: val.unit,
            isActive: val.isActive === 'true',
            treatmentCategoryId: val.treatmentCategoryId ? Number(val.treatmentCategoryId) : null,
        };
        this.loading.set(true);
        this.treatmentService.create(request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã thêm dịch vụ thành công'});
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể thêm dịch vụ, vui lòng thử lại'});
            }
        });
    }
}

