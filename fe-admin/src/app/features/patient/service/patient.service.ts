import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../../../core/model/response/success-response";
import {PatientRequest, PatientResponse} from "../model/patient.model";

@Injectable({
    providedIn: 'root',
})
export class PatientService {
    private readonly url = ENV.API_BASE_URL + "patients";
    private readonly http = inject(HttpClient);

    getAll(page = 0, size = 10, keyword = ''): Observable<SuccessResponse<any>> {
        let params = new HttpParams()
            .set('page', page)
            .set('size', size);
        if (keyword) {
            params = params.set('keyword', keyword);
        }
        return this.http.get<SuccessResponse<any>>(this.url, {params});
    }

    getById(id: string): Observable<SuccessResponse<PatientResponse>> {
        return this.http.get<SuccessResponse<PatientResponse>>(`${this.url}/${id}`);
    }

    create(request: PatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.post<SuccessResponse<PatientResponse>>(this.url, request);
    }

    update(id: string, request: PatientRequest): Observable<SuccessResponse<PatientResponse>> {
        return this.http.put<SuccessResponse<PatientResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

