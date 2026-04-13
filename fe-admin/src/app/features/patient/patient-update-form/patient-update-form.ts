import {Component, computed, inject, input, OnInit, output, signal} from '@angular/core';
import {PatientService} from "../../../core/service/patient.service";
import {form, FormField} from "@angular/forms/signals";
import {UpdatePatientRequest} from "../../../core/model/request/update-patient-request";
import {MessageService} from "primeng/api";
import {Button} from "primeng/button";
import {InputText} from "primeng/inputtext";
import {Card} from "primeng/card";
import {ProgressSpinner} from "primeng/progressspinner";

interface PatientFormData {
    code: string;
    fullName: string;
    phone: string;
    address: string;
    guardianName: string;
    guardianPhone: string;
}

@Component({
    selector: 'app-patient-update-form',
    imports: [Button, InputText, FormField, Card, ProgressSpinner],
    templateUrl: './patient-update-form.html',
    styleUrl: './patient-update-form.css',
})
export class PatientUpdateForm implements OnInit {
    private readonly patientService = inject(PatientService);
    private readonly messageService = inject(MessageService);

    patient = input.required<number>(); // truyền ID, form tự fetch

    saved = output<void>();
    cancelled = output<void>();

    loading = signal(false);
    fetching = signal(true);  // loading khi fetch data ban đầu
    dobRaw = signal('');
    genderRaw = signal('');
    errors = signal<Record<string, string>>({});
    private patientVersion = 0;

    updateForm = form(signal<PatientFormData>({
        code: '',
        fullName: '',
        phone: '',
        address: '',
        guardianName: '',
        guardianPhone: ''
    }));

    ngOnInit(): void {
        this.patientService.findById(this.patient().toString()).subscribe({
            next: res => {
                const p = res.data;
                this.patientVersion = p.version;
                this.updateForm().reset({
                    code: p.code,
                    fullName: p.fullName,
                    phone: p.phone || '',
                    address: p.address || '',
                    guardianName: p.guardianName || '',
                    guardianPhone: p.guardianPhone || '',
                });
                const d = new Date(p.dob);
                const yyyy = d.getFullYear();
                const mm = String(d.getMonth() + 1).padStart(2, '0');
                const dd = String(d.getDate()).padStart(2, '0');
                this.dobRaw.set(`${yyyy}-${mm}-${dd}`);
                this.genderRaw.set(String(p.gender));
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
        const val = this.updateForm().value();
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

        const val = this.updateForm().value();
        const request: UpdatePatientRequest = {
            code: val.code,
            fullName: val.fullName,
            dob: new Date(this.dobRaw() + 'T00:00:00'),
            gender: this.genderRaw() === 'true',
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

