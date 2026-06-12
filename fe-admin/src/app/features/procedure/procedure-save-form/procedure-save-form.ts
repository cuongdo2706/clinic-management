import {Component, inject, OnInit, output, signal} from '@angular/core';
import {ProcedureService} from "../../../core/service/procedure.service";
import {ProcedureCategoryService} from "../../../core/service/procedure-category.service";
import {CreateProcedureRequest} from "../../../core/model/request/create-procedure-request";
import {ProcedureCategoryResponse} from "../../../core/model/response/procedure-category-response";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {Select} from "primeng/select";
import {Textarea} from "primeng/textarea";

interface ProcedureFormData {
    code: string;
    name: string;
    description: string;
    price: string;
    unit: string;
    durationMinutes: string;
    isActive: boolean;
    procedureCategoryId: number | null;
}

const EMPTY_FORM: ProcedureFormData = {
    code: '',
    name: '',
    description: '',
    price: '',
    unit: '',
    durationMinutes: '30',
    isActive: true,
    procedureCategoryId: null,
};

@Component({
    selector: 'app-procedure-save-form',
    imports: [Button, InputText, Card, ReactiveFormsModule, Select, Textarea],
    templateUrl: './procedure-save-form.html',
    styleUrl: './procedure-save-form.css',
})
export class ProcedureSaveForm implements OnInit {
    private readonly procedureService = inject(ProcedureService);
    private readonly procedureCategoryService = inject(ProcedureCategoryService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    categories = signal<ProcedureCategoryResponse[]>([]);
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
        durationMinutes: this.fb.nonNullable.control(EMPTY_FORM.durationMinutes),
        isActive: this.fb.nonNullable.control(EMPTY_FORM.isActive),
        procedureCategoryId: this.fb.control<number | null>(EMPTY_FORM.procedureCategoryId),
    });

    ngOnInit(): void {
        this.procedureCategoryService.search({page: 0, size: 200}).subscribe({
            next: res => this.categories.set(res.data.content),
        });
    }

    private validate(): boolean {
        const val = this.saveForm.getRawValue();
        const errs: Record<string, string> = {};
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên dịch vụ';
        if (!val.price || isNaN(Number(val.price)) || Number(val.price) < 0)
            errs['price'] = 'Vui lòng nhập giá hợp lệ';
        if (!val.durationMinutes || isNaN(Number(val.durationMinutes)) || Number(val.durationMinutes) <= 0 || Number(val.durationMinutes) % 15 !== 0)
            errs['durationMinutes'] = 'Thời lượng phải là bội số 15 phút';
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
        const request: CreateProcedureRequest = {
            code: val.code,
            name: val.name,
            description: val.description,
            price: Number(val.price),
            unit: val.unit,
            durationMinutes: Number(val.durationMinutes),
            isActive: val.isActive,
            procedureCategoryId: val.procedureCategoryId,
        };
        this.loading.set(true);
        this.procedureService.create(request).subscribe({
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

