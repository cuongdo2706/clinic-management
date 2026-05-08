import {Component, inject, output, signal} from '@angular/core';
import {PatientService} from "../../../core/service/patient.service";
import {CreatePatientRequest} from "../../../core/model/request/create-patient-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {DatePicker} from "primeng/datepicker";
import {Select} from "primeng/select";

interface PatientFormData {
    code: string;
    fullName: string;
    dob: Date | null;
    gender: boolean | null;
    phone: string;
    address: string;
    guardianName: string;
    guardianPhone: string;
}

const EMPTY_FORM: PatientFormData = {
    code: '',
    fullName: '',
    dob: null,
    gender: null,
    phone: '',
    address: '',
    guardianName: '',
    guardianPhone: ''
};

@Component({
    selector: 'app-patient-save-form',
    imports: [Button, InputText, Card, ReactiveFormsModule, DatePicker, Select],
    templateUrl: './patient-save-form.html',
    styleUrl: './patient-save-form.css',
})
export class PatientSaveForm {
    private readonly patientService = inject(PatientService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    errors = signal<Record<string, string>>({});
    genderOptions = [
        {label: 'Nam', value: true},
        {label: 'Nữ', value: false},
    ];
    saveForm = this.fb.group({
        code: this.fb.nonNullable.control(EMPTY_FORM.code),
        fullName: this.fb.nonNullable.control(EMPTY_FORM.fullName),
        dob: this.fb.control<Date | null>(EMPTY_FORM.dob),
        gender: this.fb.control<boolean | null>(EMPTY_FORM.gender),
        phone: this.fb.nonNullable.control(EMPTY_FORM.phone),
        address: this.fb.nonNullable.control(EMPTY_FORM.address),
        guardianName: this.fb.nonNullable.control(EMPTY_FORM.guardianName),
        guardianPhone: this.fb.nonNullable.control(EMPTY_FORM.guardianPhone),
    });

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    isAdult(): boolean {
        const dob = this.saveForm.controls.dob.value;
        if (!dob) return true;
        const date = new Date(dob);
        if (isNaN(date.getTime())) return true;
        const today = new Date();
        let age = today.getFullYear() - date.getFullYear();
        const monthDiff = today.getMonth() - date.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) age--;
        return age > 14;
    }

    private validate(): boolean {
        const val = this.saveForm.getRawValue();
        const errs: Record<string, string> = {};
        if (!val.fullName?.trim()) errs['fullName'] = 'Vui lòng nhập họ và tên';
        if (!val.dob) errs['dob'] = 'Vui lòng chọn ngày sinh';
        if (val.gender === null) errs['gender'] = 'Vui lòng chọn giới tính';
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
        const val = this.saveForm.getRawValue();
        const request: CreatePatientRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: val.dob!,
            gender: val.gender,
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
