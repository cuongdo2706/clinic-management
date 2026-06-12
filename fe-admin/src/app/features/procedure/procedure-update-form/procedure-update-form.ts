import {Component, inject, input, OnInit, output, signal} from '@angular/core';
import {ProcedureService} from "../../../core/service/procedure.service";
import {ProcedureCategoryService} from "../../../core/service/procedure-category.service";
import {UpdateProcedureRequest} from "../../../core/model/request/update-procedure-request";
import {ProcedureCategoryResponse} from "../../../core/model/response/procedure-category-response";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {ProgressSpinner} from "primeng/progressspinner";
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

@Component({
    selector: 'app-procedure-update-form',
    imports: [Button, InputText, Card, ProgressSpinner, ReactiveFormsModule, Select, Textarea],
    templateUrl: './procedure-update-form.html',
    styleUrl: './procedure-update-form.css',
})
export class ProcedureUpdateForm implements OnInit {
    private readonly procedureService = inject(ProcedureService);
    private readonly procedureCategoryService = inject(ProcedureCategoryService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);

    procedure = input.required<number>();

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);
    errors = signal<Record<string, string>>({});
    categories = signal<ProcedureCategoryResponse[]>([]);
    activeOptions = [
        {label: 'Hoạt động', value: true},
        {label: 'Ngừng hoạt động', value: false},
    ];
    private procedureVersion = 0;

    updateForm = this.fb.group({
        code: this.fb.nonNullable.control(''),
        name: this.fb.nonNullable.control(''),
        description: this.fb.nonNullable.control(''),
        price: this.fb.nonNullable.control(''),
        unit: this.fb.nonNullable.control(''),
        durationMinutes: this.fb.nonNullable.control('30'),
        isActive: this.fb.nonNullable.control(true),
        procedureCategoryId: this.fb.control<number | null>(null),
    });

    ngOnInit(): void {
        this.procedureCategoryService.search({page: 0, size: 200}).subscribe({
            next: res => this.categories.set(res.data.content),
        });
        this.procedureService.findById(this.procedure()).subscribe({
            next: res => {
                const t = res.data;
                this.procedureVersion = t.version;
                this.updateForm.reset({
                    code: t.code ?? '',
                    name: t.name,
                    description: t.description ?? '',
                    price: t.price?.toString() ?? '',
                    unit: t.unit ?? '',
                    durationMinutes: String(t.durationMinutes ?? 30),
                    isActive: t.isActive,
                    procedureCategoryId: t.procedureCategory?.id ?? null,
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
        const val = this.updateForm.getRawValue();
        const errs: Record<string, string> = {};
        if (!val.name?.trim()) errs['name'] = 'Vui lòng nhập tên dịch vụ';
        if (!val.price || isNaN(Number(val.price)) || Number(val.price) < 0)
            errs['price'] = 'Vui lòng nhập giá hợp lệ';
        if (!val.durationMinutes || isNaN(Number(val.durationMinutes)) || Number(val.durationMinutes) <= 0 || Number(val.durationMinutes) % 15 !== 0)
            errs['durationMinutes'] = 'Thời lượng phải là bội số 15 phút';
        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;
        const val = this.updateForm.getRawValue();
        const request: UpdateProcedureRequest = {
            code: val.code,
            name: val.name,
            description: val.description,
            price: Number(val.price),
            unit: val.unit,
            durationMinutes: Number(val.durationMinutes),
            isActive: val.isActive,
            procedureCategoryId: val.procedureCategoryId,
            version: this.procedureVersion,
        };
        this.loading.set(true);
        this.procedureService.update(this.procedure(), request).subscribe({
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

