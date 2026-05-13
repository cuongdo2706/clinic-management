import {PaginationFormat} from "../../../core/model/request/pagination-format";

export type AppointmentStatus =
    'PENDING'
    | 'CONFIRMED'
    | 'IN_QUEUE'
    | 'IN_PROGRESS'
    | 'DONE'
    | 'CANCELLED'
    | 'NO_SHOW';

export interface AppointmentResponse {
    id: number;
    code: string;
    appointmentDate: string;
    createdAt: string;
    modifiedAt: string;
    symptom: string | null;
    note: string | null;
    status: AppointmentStatus;
    queueNumber: number | null;
    version: number;
    patientId: number;
    patientCode: string;
    patientName: string;
    patientPhone: string | null;
    dentistId: number | null;
    dentistCode: string | null;
    dentistName: string | null;
    receptionistId: number | null;
    receptionistName: string | null;
    snapshotPatientName: string | null;
    snapshotPatientPhone: string | null;
}

export interface CreateAppointmentRequest {
    patientId: number | null;
    dentistId: number | null;
    appointmentDate: string;
    symptom: string;
    note: string;
}

export interface UpdateAppointmentRequest extends CreateAppointmentRequest {
    version: number | null;
}

export interface SearchAppointmentRequest extends PaginationFormat {
    keyword: string;
    codeKeyword: string;
    patientKeyword: string;
    dentistKeyword: string;
    status: AppointmentStatus | null;
    dateFrom: string | null;
    dateTo: string | null;
    sortBy: string;
}

export interface AvailableSlotResponse {
    dentistId: number;
    date: string;
    slots: string[];
}

export interface CheckInAppointmentRequest {
    receptionistId?: number | null;
    dentistId?: number | null;
}
