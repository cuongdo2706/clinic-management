import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {ENV} from "../../environment";
import {Observable} from "rxjs";
import {SuccessResponse} from "../model/response/success-response";

export interface DashboardStats {
    totalAppointmentsToday: number;
    pendingAppointmentsToday: number;
    arrivedPatientsToday: number;
    completedAppointmentsToday: number;
    recentAppointments: RecentAppointment[];
    appointmentsByMonth: MonthlyAppointment[];
    appointmentsByDay: DailyAppointment[];
    appointmentsByHour: HourlyAppointment[];
    serviceUsage: ServiceUsage[];
}

export interface RecentAppointment {
    id: string;
    patientName: string;
    dentistName: string;
    appointmentDate: string;
    timeSlot: string;
    status: string;
}

export interface MonthlyAppointment {
    month: string;
    count: number;
}

export interface DailyAppointment {
    day: string;
    count: number;
}

export interface HourlyAppointment {
    hour: string;
    count: number;
}

export interface ServiceUsage {
    serviceName: string;
    count: number;
}

@Injectable({
    providedIn: 'root',
})
export class DashboardService {
    private readonly url = `${ENV.API_BASE_URL}/clinic/dashboard`;
    private readonly http = inject(HttpClient);

    getStats(): Observable<SuccessResponse<DashboardStats>> {
        return this.http.get<SuccessResponse<DashboardStats>>(`${this.url}/stats`);
    }
}

