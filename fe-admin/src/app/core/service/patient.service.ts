import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ENV} from "../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PatientResponse} from "../model/response/patient-response";
import {CreatePatientRequest} from "../model/request/create-patient-request";
import {UpdatePatientRequest} from "../model/request/update-patient-request";
import {PageData} from "../model/response/page-data";
import {SearchPatientRequest} from "../model/request/search-patient-request";

@Injectable({
    providedIn: 'root',
})
export class PatientService {
    private readonly url = ENV.API_BASE_URL + "clinic/patients";
    private readonly http = inject(HttpClient);

    search(searchPatientRequest: SearchPatientRequest): Observable<SuccessResponse<PageData<PatientResponse>>> {
        return this.http.post<SuccessResponse<PageData<PatientResponse>>>(`${this.url}/search`, searchPatientRequest)
    }

    findById(id: string): Observable<SuccessResponse<PatientResponse>> {
        return this.http.get<SuccessResponse<PatientResponse>>(`${this.url}/${id}`);
    }

    create(createPatientRequest: CreatePatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.post<SuccessResponse<PatientResponse>>(this.url, createPatientRequest);
    }

    update(id: string, updatePatientRequest: UpdatePatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.put<SuccessResponse<PatientResponse>>(`${this.url}/${id}`, updatePatientRequest);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }

    exportExcel(): Observable<Blob> {
        return this.http.get(`${this.url}/export`, {responseType: 'blob'});
    }
}

