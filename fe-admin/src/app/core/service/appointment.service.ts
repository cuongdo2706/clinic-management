import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {AppointmentRequest, AppointmentResponse} from "../../features/appointment/model/appointment.model";

@Injectable({
    providedIn: 'root',
})
export class AppointmentService {
    private readonly url = ENV.API_BASE_URL + "appointments";
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

    getById(id: string): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.get<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}`);
    }

    create(request: AppointmentRequest): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.post<SuccessResponse<AppointmentResponse>>(this.url, request);
    }

    update(id: string, request: AppointmentRequest): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.put<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: string): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }
}

