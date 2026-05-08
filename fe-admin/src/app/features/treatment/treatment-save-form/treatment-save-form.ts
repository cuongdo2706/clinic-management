import {Component, inject, OnInit, output, signal} from '@angular/core';
import {TreatmentService} from "../../../core/service/treatment.service";
import {TreatmentCategoryService} from "../../../core/service/treatment-category.service";
import {CreateTreatmentRequest} from "../../../core/model/request/create-treatment-request";
import {TreatmentCategoryResponse} from "../../../core/model/response/treatment-category-response";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Select} from "primeng/select";
import {Textarea} from "primeng/textarea";

interface TreatmentFormData {
    code: string;
    name: string;
    description: string;
    price: string;
    unit: string;
    isActive: boolean;
    treatmentCategoryId: number | null;
}

const EMPTY_FORM: TreatmentFormData = {
    code: '',
    name: '',
    description: '',
    price: '',
    unit: '',
    isActive: true,
    treatmentCategoryId: null,
};

@Component({
    selector: 'app-treatment-save-form',
    imports: [Button, InputText, Card, ReactiveFormsModule, Select, Textarea],
    templateUrl: './treatment-save-form.html',
    styleUrl: './treatment-save-form.css',
})
export class TreatmentSaveForm implements OnInit {
    private readonly treatmentService = inject(TreatmentService);
    private readonly treatmentCategoryService = inject(TreatmentCategoryService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    categories = signal<TreatmentCategoryResponse[]>([]);
    activeOptions = [
        {label: 'Hoạt động', value: true},
        {label: 'Ngừng hoạt động', value: false},
    ];

    saveForm = this.fb.group({
        code: this.fb.nonNullable.control(EMPTY_FORM.code),
        name: this.fb.nonNullable.control(EMPTY_FORM.name),
        description: this.fb.nonNullable.control(EMPTY_FORM.description),
        price: this.fb.nonNullable.control(EMPTY_FORM.price),
        unit: this.fb.nonNullable.control(EMPTY_FORM.unit),
        isActive: this.fb.nonNullable.control(EMPTY_FORM.isActive),
        treatmentCategoryId: this.fb.control<number | null>(EMPTY_FORM.treatmentCategoryId),
    });

    ngOnInit(): void {
        this.treatmentCategoryService.search({page: 0, size: 200}).subscribe({
            next: res => this.categories.set(res.data.content),
        });
    }

    private validate(): boolean {
        const val = this.saveForm.getRawValue();
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
        const val = this.saveForm.getRawValue();
        const request: CreateTreatmentRequest = {
            code: val.code,
            name: val.name,
            description: val.description,
            price: Number(val.price),
            unit: val.unit,
            isActive: val.isActive,
            treatmentCategoryId: val.treatmentCategoryId,
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

