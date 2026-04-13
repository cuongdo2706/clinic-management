import {Component, computed, inject, output, signal} from '@angular/core';
import {PatientService} from "../../../core/service/patient.service";
import {form, FormField} from "@angular/forms/signals";
import {CreatePatientRequest} from "../../../core/model/request/create-patient-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";

interface PatientFormData {
    code: string;
    fullName: string;
    phone: string;
    address: string;
    guardianName: string;
    guardianPhone: string;
}

const EMPTY_FORM: PatientFormData = {
    code: '',
    fullName: '',
    phone: '',
    address: '',
    guardianName: '',
    guardianPhone: ''
};

@Component({
    selector: 'app-patient-save-form',
    imports: [Button, InputText, FormField, Card],
    templateUrl: './patient-save-form.html',
    styleUrl: './patient-save-form.css',
})
export class PatientSaveForm {
    private readonly patientService = inject(PatientService);
    private readonly messageService = inject(MessageService);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    dobRaw = signal('');
    genderRaw = signal('');
    errors = signal<Record<string, string>>({});
    saveForm = form(signal<PatientFormData>(EMPTY_FORM));

    isAdult = computed(() => {
        const dobStr = this.dobRaw();
        if (!dobStr) return true;
        const dob = new Date(dobStr + 'T00:00:00');
        if (isNaN(dob.getTime())) return true;
        const today = new Date();
        let age = today.getFullYear() - dob.getFullYear();
        const m = today.getMonth() - dob.getMonth();
        if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age--;
        return age > 14;
    });

    onDobChange(event: Event): void {
        this.dobRaw.set((event.target as HTMLInputElement).value);
        this.clearError('dob');
    }

    onGenderChange(event: Event): void {
        this.genderRaw.set((event.target as HTMLSelectElement).value);
        this.clearError('gender');
    }

    private clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    private validate(): boolean {
        const val = this.saveForm().value();
        const errs: Record<string, string> = {};
        if (!val.fullName?.trim()) errs['fullName'] = 'Vui lòng nhập họ và tên';
        if (!this.dobRaw()) errs['dob'] = 'Vui lòng chọn ngày sinh';
        if (!this.genderRaw()) errs['gender'] = 'Vui lòng chọn giới tính';
        if (this.isAdult()) {
            if (!val.phone?.trim()) errs['phone'] = 'Vui lòng nhập số điện thoại';
        } else {
            if (!val.guardianName?.trim()) errs['guardianName'] = 'Vui lòng nhập tên người giám hộ';
            if (!val.guardianPhone?.trim()) errs['guardianPhone'] = 'Vui lòng nhập SĐT người giám hộ';
        }
        this.errors.set(errs);
        return Object.keys(errs).length === 0;
    }

    onSubmit(): void {
        if (!this.validate()) return;
        const val = this.saveForm().value();
        const request: CreatePatientRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: new Date(this.dobRaw() + 'T00:00:00'),
            gender: this.genderRaw() === 'true',
            phone: this.isAdult() ? val.phone : '',
            address: val.address,
            guardianName: this.isAdult() ? '' : val.guardianName,
            guardianPhone: this.isAdult() ? '' : val.guardianPhone,
        };
        this.loading.set(true);
        this.patientService.create(request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Đã thêm bệnh nhân thành công'
                });
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể thêm bệnh nhân, vui lòng thử lại'
                });
            }
        });
    }
}
