import {Component, inject, OnInit, signal} from '@angular/core';
import {FormsModule} from '@angular/forms';
import {HttpErrorResponse} from '@angular/common/http';
import {MessageService} from 'primeng/api';
import {Toast} from 'primeng/toast';
import {Button} from 'primeng/button';
import {InputText} from 'primeng/inputtext';
import {FloatLabel} from 'primeng/floatlabel';
import {Textarea} from 'primeng/textarea';
import {Select} from 'primeng/select';
import {MultiSelect} from 'primeng/multiselect';
import {InputNumber} from 'primeng/inputnumber';
import {Tag} from 'primeng/tag';
import {Divider} from 'primeng/divider';
import {TableModule} from 'primeng/table';
import {Toolbar} from 'primeng/toolbar';
import {Tooltip} from 'primeng/tooltip';
import {ExaminationService} from './service/examination.service';
import {
    AppointmentResponse,
    CheckInResponse,
    InvoiceResponse,
    MedicalRecordResponse,
    PatientResponse,
    PaymentResponse,
    PrescriptionItemForm,
    PrescriptionResponse,
} from './model/examination.model';

@Component({
    selector: 'app-examination',
    imports: [
        FormsModule,
        Toast,
        Button,
        InputText,
        FloatLabel,
        Textarea,
        Select,
        MultiSelect,
        InputNumber,
        Tag,
        Divider,
        TableModule,
        Toolbar,
        Tooltip,
    ],
    providers: [MessageService],
    templateUrl: './examination.html',
    styleUrl: './examination.css',
})
export class Examination implements OnInit {
    private readonly examService = inject(ExaminationService);
    private readonly messageService = inject(MessageService);

    // ─── Step tracking ───
    activeStep = signal(0);
    loading = signal(false);

    readonly stepsModel = [
        {label: 'Bệnh nhân', icon: 'pi pi-user'},
        {label: 'Đặt lịch', icon: 'pi pi-calendar'},
        {label: 'Tiếp nhận', icon: 'pi pi-check-circle'},
        {label: 'Khám bệnh', icon: 'pi pi-file-edit'},
        {label: 'Đơn thuốc', icon: 'pi pi-list'},
        {label: 'Thanh toán', icon: 'pi pi-wallet'},
    ];

    // ─── Step 0: Patient ───
    patientMode = signal<'search' | 'create'>('search');
    patientSearchKeyword = '';
    patientSearchResults = signal<PatientResponse[]>([]);
    selectedPatient = signal<PatientResponse | null>(null);
    walkInForm = {fullName: '', phone: '', dob: '', gender: null as boolean | null, email: '', address: ''};

    // ─── Step 1: Appointment ───
    dentistOptions = signal<any[]>([]);
    appointmentForm = {
        dentistId: null as number | null,
        appointmentDate: '',
        startTime: '09:00',
        endTime: '10:00',
        note: ''
    };
    createdAppointment = signal<AppointmentResponse | null>(null);

    // ─── Step 2: Check-in ───
    checkInNote = '';
    checkInResult = signal<CheckInResponse | null>(null);

    // ─── Step 3: Medical Record ───
    serviceOptions = signal<any[]>([]);
    medicalRecordForm = {
        chiefComplaint: '',
        diagnosis: '',
        treatmentPlan: '',
        notes: '',
        serviceIds: [] as number[]
    };
    medicalRecordResult = signal<MedicalRecordResponse | null>(null);

    // ─── Step 4: Prescription ───
    medicineOptions = signal<any[]>([]);
    prescriptionNote = '';
    prescriptionItems: PrescriptionItemForm[] = [];
    prescriptionResult = signal<PrescriptionResponse | null>(null);
    prescriptionSkipped = signal(false);

    // ─── Step 5: Invoice & Payment ───
    discountAmount = 0;
    invoiceResult = signal<InvoiceResponse | null>(null);
    paymentForm = {amount: 0, paymentMethod: 'CASH', note: ''};
    paymentResult = signal<PaymentResponse | null>(null);
    flowCompleted = signal(false);

    // ─── Static Options ───
    readonly genderOptions = [
        {label: 'Nam', value: true},
        {label: 'Nữ', value: false},
    ];
    readonly paymentMethods = [
        {label: 'Tiền mặt', value: 'CASH'},
        {label: 'Thẻ', value: 'CARD'},
        {label: 'Chuyển khoản', value: 'TRANSFER'},
        {label: 'MoMo', value: 'MOMO'},
        {label: 'ZaloPay', value: 'ZALO_PAY'},
        {label: 'VNPay', value: 'VNPAY'},
    ];

    // ══════════════════════════════
    //  LIFECYCLE
    // ══════════════════════════════

    ngOnInit() {
        this.loadLookupData();
    }

    loadLookupData() {
        this.examService.getDentists().subscribe({
            next: res => {
                const data = res.data?.content ?? res.data ?? [];
                this.dentistOptions.set(Array.isArray(data) ? data.map((d: any) => ({
                    label: `${d.fullName} (${d.code})`,
                    value: d.id
                })) : []);
            },
            error: () => {
            }
        });

        this.examService.getServices().subscribe({
            next: res => {
                const data = res.data?.content ?? res.data ?? [];
                this.serviceOptions.set(Array.isArray(data) ? data.map((s: any) => ({
                    label: `${s.name} — ${Number(s.price || 0).toLocaleString('vi-VN')}đ`,
                    value: s.id
                })) : []);
            },
            error: () => {
            }
        });

        this.examService.getMedicines().subscribe({
            next: res => {
                const data = res.data?.content ?? res.data ?? [];
                this.medicineOptions.set(Array.isArray(data) ? data.map((m: any) => ({
                    label: `${m.name} (${m.unit || ''})`,
                    value: m.id
                })) : []);
            },
            error: () => {
            }
        });
    }

    // ══════════════════════════════
    //  STEP 0 — Patient
    // ══════════════════════════════

    searchPatients() {
        if (!this.patientSearchKeyword.trim()) return;
        this.loading.set(true);
        this.examService.searchPatients(this.patientSearchKeyword).subscribe({
            next: res => {
                const data = res.data?.content ?? res.data ?? [];
                this.patientSearchResults.set(Array.isArray(data) ? data : []);
                this.loading.set(false);
            },
            error: () => this.loading.set(false)
        });
    }

    selectPatient(patient: PatientResponse) {
        this.selectedPatient.set(patient);
        this.toast('success', 'Đã chọn', `Bệnh nhân: ${patient.fullName}`);
    }

    createWalkInPatient() {
        if (!this.walkInForm.fullName.trim() || !this.walkInForm.phone.trim()) {
            this.toast('warn', 'Thiếu thông tin', 'Họ tên và số điện thoại là bắt buộc');
            return;
        }
        this.loading.set(true);
        this.examService.createWalkInPatient({
            fullName: this.walkInForm.fullName,
            phone: this.walkInForm.phone,
            dob: this.walkInForm.dob || undefined,
            gender: this.walkInForm.gender ?? undefined,
            email: this.walkInForm.email || undefined,
            address: this.walkInForm.address || undefined,
        }).subscribe({
            next: res => {
                this.selectedPatient.set(res.data);
                this.loading.set(false);
                this.toast('success', 'Thành công', `Tạo bệnh nhân: ${res.data.fullName} (${res.data.code})`);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể tạo bệnh nhân');
            }
        });
    }

    goToStep(step: number) {
        this.activeStep.set(step);
    }

    // ══════════════════════════════
    //  STEP 1 — Appointment
    // ══════════════════════════════

    createAppointment() {
        const patient = this.selectedPatient();
        if (!patient || !this.appointmentForm.dentistId || !this.appointmentForm.appointmentDate) {
            this.toast('warn', 'Thiếu thông tin', 'Vui lòng chọn nha sĩ và ngày hẹn');
            return;
        }

        this.loading.set(true);
        let dateStr = this.appointmentForm.appointmentDate;
        if (typeof dateStr === 'object' && dateStr) {
            const d = dateStr as unknown as Date;
            dateStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
        }

        this.examService.createAppointment({
            patientId: patient.id,
            dentistId: this.appointmentForm.dentistId,
            appointmentDate: dateStr,
            startTime: this.appointmentForm.startTime,
            endTime: this.appointmentForm.endTime,
            note: this.appointmentForm.note || undefined,
        }).subscribe({
            next: res => {
                this.createdAppointment.set(res.data);
                this.loading.set(false);
                this.toast('success', 'Thành công', `Đặt lịch: ${res.data.code}`);
                this.activeStep.set(2);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể đặt lịch');
            }
        });
    }

    // ══════════════════════════════
    //  STEP 2 — Check-in
    // ══════════════════════════════

    performCheckIn() {
        const appt = this.createdAppointment();
        if (!appt) return;

        this.loading.set(true);

        const doCheckIn = () => {
            this.examService.checkIn({
                appointmentId: appt.id,
                note: this.checkInNote || undefined
            }).subscribe({
                next: res => {
                    this.checkInResult.set(res.data);
                    this.loading.set(false);
                    this.toast('success', 'Tiếp nhận thành công', `Số thứ tự: ${res.data.queueNumber}`);
                    this.activeStep.set(3);
                },
                error: (err: HttpErrorResponse) => {
                    this.loading.set(false);
                    this.toast('error', 'Lỗi check-in', err.error?.message || 'Không thể tiếp nhận');
                }
            });
        };

        // If PENDING → confirm first then check-in
        if (appt.status === 'PENDING') {
            this.examService.confirmAppointment(appt.id).subscribe({
                next: res => {
                    this.createdAppointment.set(res.data);
                    doCheckIn();
                },
                error: (err: HttpErrorResponse) => {
                    this.loading.set(false);
                    this.toast('error', 'Lỗi xác nhận', err.error?.message || 'Không thể xác nhận lịch hẹn');
                }
            });
        } else {
            doCheckIn();
        }
    }

    // ══════════════════════════════
    //  STEP 3 — Medical Record
    // ══════════════════════════════

    createMedicalRecord() {
        const appt = this.createdAppointment();
        if (!appt) return;

        this.loading.set(true);
        this.examService.createMedicalRecord({
            appointmentId: appt.id,
            chiefComplaint: this.medicalRecordForm.chiefComplaint || undefined,
            diagnosis: this.medicalRecordForm.diagnosis || undefined,
            treatmentPlan: this.medicalRecordForm.treatmentPlan || undefined,
            notes: this.medicalRecordForm.notes || undefined,
            serviceIds: this.medicalRecordForm.serviceIds.length > 0 ? this.medicalRecordForm.serviceIds : undefined,
        }).subscribe({
            next: res => {
                this.medicalRecordResult.set(res.data);
                this.loading.set(false);
                this.toast('success', 'Thành công', `Tạo bệnh án: ${res.data.code}`);
                this.activeStep.set(4);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể tạo bệnh án');
            }
        });
    }

    // ══════════════════════════════
    //  STEP 4 — Prescription
    // ══════════════════════════════

    addPrescriptionItem() {
        this.prescriptionItems = [
            ...this.prescriptionItems,
            {medicineId: null, quantity: 1, dosage: '', instruction: ''}
        ];
    }

    removePrescriptionItem(index: number) {
        this.prescriptionItems = this.prescriptionItems.filter((_, i) => i !== index);
    }

    createPrescription() {
        const mr = this.medicalRecordResult();
        if (!mr) return;
        if (this.prescriptionItems.length === 0) {
            this.toast('warn', 'Chưa có thuốc', 'Vui lòng thêm ít nhất 1 loại thuốc hoặc bỏ qua');
            return;
        }
        const invalid = this.prescriptionItems.some(i => !i.medicineId || !i.quantity);
        if (invalid) {
            this.toast('warn', 'Thiếu thông tin', 'Vui lòng chọn thuốc và nhập số lượng');
            return;
        }

        this.loading.set(true);
        this.examService.createPrescription({
            medicalRecordId: mr.id,
            note: this.prescriptionNote || undefined,
            items: this.prescriptionItems.map(i => ({
                medicineId: i.medicineId!,
                quantity: i.quantity,
                dosage: i.dosage || undefined,
                instruction: i.instruction || undefined,
            }))
        }).subscribe({
            next: res => {
                this.prescriptionResult.set(res.data);
                this.loading.set(false);
                this.toast('success', 'Thành công', `Kê đơn: ${res.data.code}`);
                this.activeStep.set(5);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể kê đơn');
            }
        });
    }

    skipPrescription() {
        this.prescriptionSkipped.set(true);
        this.activeStep.set(5);
    }

    // ══════════════════════════════
    //  STEP 5 — Invoice & Payment
    // ══════════════════════════════

    createInvoice() {
        const appt = this.createdAppointment();
        if (!appt) return;

        this.loading.set(true);
        this.examService.createInvoice({
            appointmentId: appt.id,
            discountAmount: this.discountAmount || undefined,
        }).subscribe({
            next: res => {
                this.invoiceResult.set(res.data);
                this.paymentForm.amount = res.data.finalAmount;
                this.loading.set(false);
                this.toast('success', 'Thành công', `Tạo hóa đơn: ${res.data.code}`);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể tạo hóa đơn');
            }
        });
    }

    createPayment() {
        const inv = this.invoiceResult();
        if (!inv) return;
        if (!this.paymentForm.amount || this.paymentForm.amount <= 0) {
            this.toast('warn', 'Thiếu thông tin', 'Số tiền thanh toán phải > 0');
            return;
        }

        this.loading.set(true);
        this.examService.createPayment({
            invoiceId: inv.id,
            amount: this.paymentForm.amount,
            paymentMethod: this.paymentForm.paymentMethod,
            note: this.paymentForm.note || undefined,
        }).subscribe({
            next: res => {
                this.paymentResult.set(res.data);
                this.loading.set(false);
                if (res.data.invoiceStatus === 'PAID') {
                    this.flowCompleted.set(true);
                }
                this.toast('success', 'Thanh toán thành công', `${res.data.code} — ${res.data.invoiceStatusLabel}`);
            },
            error: (err: HttpErrorResponse) => {
                this.loading.set(false);
                this.toast('error', 'Lỗi', err.error?.message || 'Không thể thanh toán');
            }
        });
    }

    // ══════════════════════════════
    //  RESET
    // ══════════════════════════════

    resetFlow() {
        this.activeStep.set(0);
        this.loading.set(false);
        this.patientMode.set('search');
        this.patientSearchKeyword = '';
        this.patientSearchResults.set([]);
        this.selectedPatient.set(null);
        this.walkInForm = {fullName: '', phone: '', dob: '', gender: null, email: '', address: ''};

        this.appointmentForm = {dentistId: null, appointmentDate: '', startTime: '09:00', endTime: '10:00', note: ''};
        this.createdAppointment.set(null);

        this.checkInNote = '';
        this.checkInResult.set(null);

        this.medicalRecordForm = {chiefComplaint: '', diagnosis: '', treatmentPlan: '', notes: '', serviceIds: []};
        this.medicalRecordResult.set(null);

        this.prescriptionNote = '';
        this.prescriptionItems = [];
        this.prescriptionResult.set(null);
        this.prescriptionSkipped.set(false);

        this.discountAmount = 0;
        this.invoiceResult.set(null);
        this.paymentForm = {amount: 0, paymentMethod: 'CASH', note: ''};
        this.paymentResult.set(null);
        this.flowCompleted.set(false);
    }

    // ══════════════════════════════
    //  HELPERS
    // ══════════════════════════════

    private toast(severity: string, summary: string, detail: string) {
        this.messageService.add({severity, summary, detail});
    }

    formatVND(amount: number | undefined): string {
        if (!amount && amount !== 0) return '0đ';
        return new Intl.NumberFormat('vi-VN').format(amount) + 'đ';
    }
}





