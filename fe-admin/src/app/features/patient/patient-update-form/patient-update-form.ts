import {Component, inject, input, OnInit, output, signal} from '@angular/core';
import {PatientService} from "../../../core/service/patient.service";
import {UpdatePatientRequest} from "../../../core/model/request/update-patient-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {ProgressSpinner} from "primeng/progressspinner";
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

@Component({
    selector: 'app-patient-update-form',
    imports: [Button, InputText, Card, ProgressSpinner, ReactiveFormsModule, DatePicker, Select],
    templateUrl: './patient-update-form.html',
    styleUrl: './patient-update-form.css',
})
export class PatientUpdateForm implements OnInit {
    private readonly patientService = inject(PatientService);
    private readonly messageService = inject(MessageService);
    private readonly fb = inject(FormBuilder);

    patient = input.required<number>(); // truyền ID, form tự fetch

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);  // loading khi fetch data ban đầu
    errors = signal<Record<string, string>>({});
    private patientVersion = 0;
    genderOptions = [
        {label: 'Nam', value: true},
        {label: 'Nữ', value: false},
    ];

    updateForm = this.fb.group({
        code: this.fb.nonNullable.control(''),
        fullName: this.fb.nonNullable.control(''),
        dob: this.fb.control<Date | null>(null),
        gender: this.fb.control<boolean | null>(null),
        phone: this.fb.nonNullable.control(''),
        address: this.fb.nonNullable.control(''),
        guardianName: this.fb.nonNullable.control(''),
        guardianPhone: this.fb.nonNullable.control('')
    });

    ngOnInit(): void {
        this.patientService.findById(this.patient().toString()).subscribe({
            next: res => {
                const p = res.data;
                this.patientVersion = p.version;
                this.updateForm.reset({
                    code: p.code,
                    fullName: p.fullName,
                    dob: p.dob ? new Date(p.dob) : null,
                    gender: p.gender,
                    phone: p.phone || '',
                    address: p.address || '',
                    guardianName: p.guardianName || '',
                    guardianPhone: p.guardianPhone || '',
                });
                this.fetching.set(false);
            },
            error: () => {
                this.fetching.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể tải thông tin bệnh nhân'
                });
                this.cancelled.emit();
            }
        });
    }

    isAdult(): boolean {
        const dob = this.updateForm.controls.dob.value;
        if (!dob) return true;
        const date = new Date(dob);
        if (isNaN(date.getTime())) return true;
        const today = new Date();
        let age = today.getFullYear() - date.getFullYear();
        const monthDiff = today.getMonth() - date.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < date.getDate())) age--;
        return age > 14;
    }

    clearError(key: string): void {
        const errs = {...this.errors()};
        delete errs[key];
        this.errors.set(errs);
    }

    private validate(): boolean {
        const val = this.updateForm.getRawValue();
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

        const val = this.updateForm.getRawValue();
        const request: UpdatePatientRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: val.dob!,
            gender: val.gender!,
            phone: this.isAdult() ? val.phone : '',
            address: val.address,
            guardianName: this.isAdult() ? '' : val.guardianName,
            guardianPhone: this.isAdult() ? '' : val.guardianPhone,
            version: this.patientVersion,
        };
        this.loading.set(true);
        this.patientService.update(this.patient().toString(), request).subscribe({
            next: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'success',
                    summary: 'Thành công',
                    detail: 'Đã cập nhật bệnh nhân thành công'
                });
                this.saved.emit();
            },
            error: () => {
                this.loading.set(false);
                this.messageService.add({
                    severity: 'error',
                    summary: 'Lỗi',
                    detail: 'Không thể cập nhật bệnh nhân, vui lòng thử lại'
                });
            }
        });
    }
}

