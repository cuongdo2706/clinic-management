import {inject, Injectable} from '@angular/core';
import {ENV} from "../../environment";
import {HttpClient} from "@angular/common/http";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {TreatmentResponse} from "../model/response/treatment-response";
import {SearchTreatmentRequest} from "../model/request/search-treatment-request";
import {CreateTreatmentRequest} from "../model/request/create-treatment-request";
import {UpdateTreatmentRequest} from "../model/request/update-treatment-request";

@Injectable({
    providedIn: 'root',
})
export class TreatmentService {
    private readonly url = ENV.API_BASE_URL + "clinic/treatments";
    private readonly http = inject(HttpClient);

    search(request: SearchTreatmentRequest): Observable<SuccessResponse<PageData<TreatmentResponse>>> {
        return this.http.post<SuccessResponse<PageData<TreatmentResponse>>>(`${this.url}/search`, request);
    }

    findById(id: number): Observable<SuccessResponse<TreatmentResponse>> {
        return this.http.get<SuccessResponse<TreatmentResponse>>(`${this.url}/${id}`);
    }

    create(request: CreateTreatmentRequest): Observable<SuccessResponse<TreatmentResponse>> {
        return this.http.post<SuccessResponse<TreatmentResponse>>(this.url, request);
    }

    update(id: number, request: UpdateTreatmentRequest): Observable<SuccessResponse<TreatmentResponse>> {
        return this.http.put<SuccessResponse<TreatmentResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: number): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

