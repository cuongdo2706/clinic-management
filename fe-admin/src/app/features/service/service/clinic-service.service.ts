import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../../../core/model/response/success-response";
import {ClinicServiceRequest, ClinicServiceResponse} from "../model/clinic-service.model";

@Injectable({
    providedIn: 'root',
})
export class ClinicServiceService {
    private readonly url = ENV.API_BASE_URL + "services";
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

    getById(id: string): Observable<SuccessResponse<ClinicServiceResponse>> {
        return this.http.get<SuccessResponse<ClinicServiceResponse>>(`${this.url}/${id}`);
    }

    create(request: ClinicServiceRequest): Observable<SuccessResponse<ClinicServiceResponse>> {
        return this.http.post<SuccessResponse<ClinicServiceResponse>>(this.url, request);
    }

    update(id: string, request: ClinicServiceRequest): Observable<SuccessResponse<ClinicServiceResponse>> {
        return this.http.put<SuccessResponse<ClinicServiceResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

