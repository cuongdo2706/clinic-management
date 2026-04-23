import {Component, inject, input, OnInit, output, signal} from '@angular/core';
import {TreatmentService} from "../../../core/service/treatment.service";
import {TreatmentCategoryService} from "../../../core/service/treatment-category.service";
import {form, FormField} from "@angular/forms/signals";
import {UpdateTreatmentRequest} from "../../../core/model/request/update-treatment-request";
import {TreatmentCategoryResponse} from "../../../core/model/response/treatment-category-response";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {ProgressSpinner} from "primeng/progressspinner";
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

@Component({
    selector: 'app-treatment-update-form',
    imports: [Button, InputText, FormField, Card, ProgressSpinner, FormsModule],
    templateUrl: './treatment-update-form.html',
    styleUrl: './treatment-update-form.css',
})
export class TreatmentUpdateForm implements OnInit {
    private readonly treatmentService = inject(TreatmentService);
    private readonly treatmentCategoryService = inject(TreatmentCategoryService);
    private readonly messageService = inject(MessageService);

    treatment = input.required<number>();

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);
    errors = signal<Record<string, string>>({});
    categories = signal<TreatmentCategoryResponse[]>([]);
    private treatmentVersion = 0;

    updateForm = form(signal<TreatmentFormData>({
        code: '', name: '', description: '', price: '', unit: '', isActive: 'true', treatmentCategoryId: '',
    }));

    ngOnInit(): void {
        this.treatmentCategoryService.search({page: 0, size: 200}).subscribe({
            next: res => this.categories.set(res.data.content),
        });
        this.treatmentService.findById(this.treatment()).subscribe({
            next: res => {
                const t = res.data;
                this.treatmentVersion = t.version;
                this.updateForm().reset({
                    code: t.code ?? '',
                    name: t.name,
                    description: t.description ?? '',
                    price: t.price?.toString() ?? '',
                    unit: t.unit ?? '',
                    isActive: t.isActive ? 'true' : 'false',
                    treatmentCategoryId: t.treatmentCategory?.id?.toString() ?? '',
                });
                this.fetching.set(false);
            },
            error: () => {
                this.fetching.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể tải thông tin dịch vụ'});
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
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên dịch vụ';
        if (!val.price || isNaN(Number(val.price)) || Number(val.price) < 0)
            errs['price'] = 'Vui lòng nhập giá hợp lệ';
        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;
        const val = this.updateForm().value();
        const request: UpdateTreatmentRequest = {
            code: val.code,
            name: val.name,
            description: val.description,
            price: Number(val.price),
            unit: val.unit,
            isActive: val.isActive === 'true',
            treatmentCategoryId: val.treatmentCategoryId ? Number(val.treatmentCategoryId) : null,
            version: this.treatmentVersion,
        };
        this.loading.set(true);
        this.treatmentService.update(this.treatment(), request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật dịch vụ thành công'});
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không thể cập nhật dịch vụ, vui lòng thử lại'});
            }
        });
    }
}

