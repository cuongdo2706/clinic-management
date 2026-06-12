import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {ENV} from "../../environment";
import {SuccessResponse} from "../model/response/success-response";
import {TreatmentDetailResponse} from "../model/response/patient-detail-response";
import {CreateTreatmentRequest, UpdateTreatmentRequest} from "../model/request/treatment-request";

@Injectable({
    providedIn: 'root',
})
export class TreatmentService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/treatments`;
    private readonly http = inject(HttpClient);

    findById(id: number): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.get<SuccessResponse<TreatmentDetailResponse>>(`${this.url}/${id}`);
    }

    create(request: CreateTreatmentRequest): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.post<SuccessResponse<TreatmentDetailResponse>>(this.url, request);
    }

    update(id: number, request: UpdateTreatmentRequest): Observable<SuccessResponse<TreatmentDetailResponse>> {
        return this.http.put<SuccessResponse<TreatmentDetailResponse>>(`${this.url}/${id}`, request);
    }
}
