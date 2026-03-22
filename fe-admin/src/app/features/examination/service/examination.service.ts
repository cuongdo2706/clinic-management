import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ENV} from '../../../environment';
import {SuccessResponse} from '../../../core/model/response/success-response';
import {
    AppointmentResponse,
    CheckInRequest,
    CheckInResponse,
    CreateAppointmentRequest,
    CreateInvoiceRequest,
    CreateMedicalRecordRequest,
    CreatePaymentRequest,
    CreatePrescriptionRequest,
    InvoiceResponse,
    MedicalRecordResponse,
    PatientResponse,
    PaymentResponse,
    PrescriptionResponse,
    WalkInPatientRequest,
} from '../model/examination.model';

@Injectable({
    providedIn: 'root',
})
export class ExaminationService {
    private readonly url = ENV.API_BASE_URL + 'examination';
    private readonly http = inject(HttpClient);

    // ══════════════════════════════
    //  Examination Flow APIs
    // ══════════════════════════════

    createWalkInPatient(req: WalkInPatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.post<SuccessResponse<PatientResponse>>(`${this.url}/walk-in-patients`, req);
    }

    createAppointment(req: CreateAppointmentRequest): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.post<SuccessResponse<AppointmentResponse>>(`${this.url}/appointments`, req);
    }

    confirmAppointment(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.put<SuccessResponse<AppointmentResponse>>(`${this.url}/appointments/${id}/confirm`, {});
    }

    checkIn(req: CheckInRequest): Observable<SuccessResponse<CheckInResponse>> {
        return this.http.post<SuccessResponse<CheckInResponse>>(`${this.url}/check-in`, req);
    }

    createMedicalRecord(req: CreateMedicalRecordRequest): Observable<SuccessResponse<MedicalRecordResponse>> {
        return this.http.post<SuccessResponse<MedicalRecordResponse>>(`${this.url}/medical-records`, req);
    }

    createPrescription(req: CreatePrescriptionRequest): Observable<SuccessResponse<PrescriptionResponse>> {
        return this.http.post<SuccessResponse<PrescriptionResponse>>(`${this.url}/prescriptions`, req);
    }

    createInvoice(req: CreateInvoiceRequest): Observable<SuccessResponse<InvoiceResponse>> {
        return this.http.post<SuccessResponse<InvoiceResponse>>(`${this.url}/invoices`, req);
    }

    createPayment(req: CreatePaymentRequest): Observable<SuccessResponse<PaymentResponse>> {
        return this.http.post<SuccessResponse<PaymentResponse>>(`${this.url}/payments`, req);
    }

    // ══════════════════════════════
    //  Lookup / Search APIs
    // ══════════════════════════════

    searchPatients(keyword: string): Observable<SuccessResponse<any>> {
        return this.http.get<SuccessResponse<any>>(`${ENV.API_BASE_URL}patients`, {
            params: {keyword, page: '0', size: '20'}
        });
    }

    getDentists(): Observable<SuccessResponse<any>> {
        return this.http.get<SuccessResponse<any>>(`${ENV.API_BASE_URL}staffs`, {
            params: {type: 'DENTIST'}
        });
    }

    getServices(): Observable<SuccessResponse<any>> {
        return this.http.get<SuccessResponse<any>>(`${ENV.API_BASE_URL}services`, {
            params: {page: '0', size: '100'}
        });
    }

    getMedicines(): Observable<SuccessResponse<any>> {
        return this.http.get<SuccessResponse<any>>(`${ENV.API_BASE_URL}medicines`, {
            params: {page: '0', size: '100'}
        });
    }
}

