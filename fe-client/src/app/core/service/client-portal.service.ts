import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ENV } from '../../environment';
import { ClientBookingRequest } from '../model/request/client-booking-request';
import { UpdatePatientProfileRequest } from '../model/request/update-patient-profile-request';
import { AppointmentResponse } from '../model/response/appointment-response';
import { DentistResponse } from '../model/response/dentist-response';
import { HealthRecordResponse } from '../model/response/health-record-response';
import { PatientProfileResponse } from '../model/response/patient-profile-response';
import { SlotResponse } from '../model/response/slot-response';
import { SuccessResponse } from '../model/response/success-response';

@Injectable({
  providedIn: 'root',
})
export class ClientPortalService {
  private readonly http = inject(HttpClient);
  private readonly url = `${ENV.API_BASE_URL}/client`;

  getProfile(): Observable<SuccessResponse<PatientProfileResponse>> {
    return this.http.get<SuccessResponse<PatientProfileResponse>>(`${this.url}/me`);
  }

  updateProfile(
    request: UpdatePatientProfileRequest,
  ): Observable<SuccessResponse<PatientProfileResponse>> {
    return this.http.put<SuccessResponse<PatientProfileResponse>>(`${this.url}/me`, request);
  }

  getDentists(): Observable<SuccessResponse<DentistResponse[]>> {
    return this.http.get<SuccessResponse<DentistResponse[]>>(`${this.url}/booking/dentists`);
  }

  getAvailableSlots(
    dentistId: number,
    date: string,
    durationMinutes: number,
  ): Observable<SuccessResponse<SlotResponse>> {
    const params = new HttpParams()
      .set('dentistId', dentistId)
      .set('date', date)
      .set('durationMinutes', durationMinutes);
    return this.http.get<SuccessResponse<SlotResponse>>(`${this.url}/booking/available-slots`, {
      params,
    });
  }

  bookAppointment(
    request: ClientBookingRequest,
  ): Observable<SuccessResponse<AppointmentResponse>> {
    return this.http.post<SuccessResponse<AppointmentResponse>>(`${this.url}/appointments`, request);
  }

  getAppointments(): Observable<SuccessResponse<AppointmentResponse[]>> {
    return this.http.get<SuccessResponse<AppointmentResponse[]>>(`${this.url}/appointments`);
  }

  cancelAppointment(id: number): Observable<SuccessResponse<AppointmentResponse>> {
    return this.http.patch<SuccessResponse<AppointmentResponse>>(
      `${this.url}/appointments/${id}/cancel`,
      {},
    );
  }

  getHealthRecords(): Observable<SuccessResponse<HealthRecordResponse[]>> {
    return this.http.get<SuccessResponse<HealthRecordResponse[]>>(`${this.url}/health-records`);
  }
}
