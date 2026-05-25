import {inject, Injectable} from '@angular/core';
import {HttpClient, HttpParams} from "@angular/common/http";
import {ENV} from "../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";
import {PageData} from "../model/response/page-data";
import {
    AppointmentResponse,
    AvailableSlotResponse,
    CheckInAppointmentRequest,
    CreateAppointmentRequest,
    SearchAppointmentRequest,
    UpdateAppointmentRequest
} from "../../features/appointment/model/appointment.model";

@Injectable({
    providedIn: 'root',
})
export class AppointmentService {
    private readonly url = ENV.API_BASE_URL + "clinic/appointments";
    private readonly http = inject(HttpClient);

    search(request: SearchAppointmentRequest): Observable<SuccessResponse<PageData<AppointmentResponse>>> {
        return this.http.post<SuccessResponse<PageData<AppointmentResponse>>>(`${this.url}/search`, request);
    }

    getById(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.get<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}`);
    }

    create(request: CreateAppointmentRequest): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.post<SuccessResponse<AppointmentResponse>>(this.url, request);
    }

    update(id: number, request: UpdateAppointmentRequest): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.put<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}`, request);
    }

    delete(id: number): Observable<SuccessResponse<void>> {
        return this.http.delete<SuccessResponse<void>>(`${this.url}/${id}`);
    }

    confirm(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/confirm`, {});
    }

    checkIn(id: number, request: CheckInAppointmentRequest = {}): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/check-in`, request);
    }

    start(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/start`, {});
    }

    done(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/done`, {});
    }

    cancel(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/cancel`, {});
    }

    noShow(id: number): Observable<SuccessResponse<AppointmentResponse>> {
        return this.http.patch<SuccessResponse<AppointmentResponse>>(`${this.url}/${id}/no-show`, {});
    }

    getAvailableSlots(dentistId: number, date: string, estimatedDurationMinutes: number): Observable<SuccessResponse<AvailableSlotResponse>> {
        const params = new HttpParams()
            .set('dentistId', dentistId)
            .set('date', date)
            .set('estimatedDurationMinutes', estimatedDurationMinutes);
        return this.http.get<SuccessResponse<AvailableSlotResponse>>(`${this.url}/available-slots`, {params});
    }
}
