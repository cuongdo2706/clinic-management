import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ENV} from '../../environment';
import {SuccessResponse} from '../model/response/success-response';
import {PageData} from '../model/response/page-data';
import {StaffResponse} from '../model/response/staff-response';
import {PatientDetailResponse, TreatmentDetailResponse} from '../model/response/patient-detail-response';
import {ProcedureResponse} from '../model/response/procedure-response';
import {MedicineResponse} from '../model/response/medicine-response';
import {SearchProcedureRequest} from '../model/request/search-procedure-request';
import {SearchMedicineRequest} from '../model/request/search-medicine-request';
import {CreateTreatmentRequest, UpdateTreatmentRequest} from '../model/request/treatment-request';
import {AppointmentResponse, SearchAppointmentRequest} from '../../features/appointment/model/appointment.model';

@Injectable({
    providedIn: 'root',
})
export class ExaminationService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/examinations`;
    private readonly http = inject(HttpClient);

    currentDentist(): Observable<SuccessResponse<StaffResponse>> {
        return this.http.get<SuccessResponse<StaffResponse>>(`${this.url}/me`);
    }

    searchAppointments(request: SearchAppointmentRequest): Observable<SuccessResponse<PageData<AppointmentResponse>>> {
        return this.http.post<SuccessResponse<PageData<AppointmentResponse>>>(`${this.url}/search`, request);
    }

    start(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/start`, {});
    }

    done(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/done`, {});
    }

    patientDetail(id: number): Observable<SuccessResponse<PatientDetailResponse>> {
        return this.http.get<SuccessResponse<PatientDetailResponse>>(`${this.url}/patients/${id}/detail`);
    }

    searchProcedures(request: SearchProcedureRequest): Observable<SuccessResponse<PageData<ProcedureResponse>>> {
        return this.http.post<SuccessResponse<PageData<ProcedureResponse>>>(`${this.url}/procedures/search`, request);
    }

    searchMedicines(request: SearchMedicineRequest): Observable<SuccessResponse<PageData<MedicineResponse>>> {
        return this.http.post<SuccessResponse<PageData<MedicineResponse>>>(`${this.url}/medicines/search`, request);
    }

    findTreatmentById(id: number): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.get<SuccessResponse<TreatmentDetailResponse>>(`${this.url}/treatments/${id}`);
    }

    createTreatment(request: CreateTreatmentRequest): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.post<SuccessResponse<TreatmentDetailResponse>>(`${this.url}/treatments`, request);
    }

    updateTreatment(id: number, request: UpdateTreatmentRequest): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.put<SuccessResponse<TreatmentDetailResponse>>(`${this.url}/treatments/${id}`, request);
    }
}
