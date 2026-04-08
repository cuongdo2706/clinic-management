import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../../../core/model/response/success-response";
import {PatientResponse} from "../../../core/model/response/patient-response";
import {CreatePatientRequest} from "../../../core/model/request/create-patient-request";
import {UpdatePatientRequest} from "../../../core/model/request/update-patient-request";
import {PageData} from "../../../core/model/response/page-data";
import {SearchPatientRequest} from "../../../core/model/request/search-patient-request";
@Injectable({
    providedIn: 'root',
})
export class PatientService {
    private readonly url = ENV.API_BASE_URL + "patients";
    private readonly http = inject(HttpClient);

    search(searchPatientRequest:SearchPatientRequest): Observable<SuccessResponse<PageData<PatientResponse>>> {
        return this.http.post<SuccessResponse<PageData<PatientResponse>>>(`${this.url}/search`,searchPatientRequest)
    }

    findById(id: string): Observable<SuccessResponse<PatientResponse>> {
        return this.http.get<SuccessResponse<PatientResponse>>(`${this.url}/${id}`);
    }

    create(createPatientRequest:CreatePatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.post<SuccessResponse<PatientResponse>>(this.url, createPatientRequest);
    }

    update(id: string, updatePatientRequest:UpdatePatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.put<SuccessResponse<PatientResponse>>(`${this.url}/${id}`, updatePatientRequest);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }

    exportExcel(): Observable<Blob> {
        return this.http.get(`${this.url}/export`, {responseType: 'blob'});
    }
}

