import {PaginationFormat} from "../../../core/model/request/pagination-format";

export type AppointmentStatus =
    'PENDING'
    | 'CONFIRMED'
    | 'IN_PROGRESS'
    | 'COMPLETED'
    | 'CANCELLED';

export type AppointmentArrivalStatus =
    'NOT_ARRIVED'
    | 'ARRIVED'
    | 'NO_SHOW';

export type AppointmentSortOption =
    'APPOINTMENT_DATE'
    | 'APPOINTMENT_DATE_DESC'
    | 'CREATED_AT'
    | 'CREATED_AT_DESC';

export interface AppointmentResponse {
    id: number;
    code: string;
    appointmentDate: string;
    estimatedDurationMinutes: number;
    createdAt: string;
    modifiedAt: string;
    symptom: string | null;
    note: string | null;
    status: AppointmentStatus;
    arrivalStatus: AppointmentArrivalStatus;
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
    estimatedDurationMinutes: number | null;
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
    sortBy: AppointmentSortOption;
}

export interface AvailableSlotResponse {
    dentistId: number;
    date: string;
    durationMinutes: number;
    slotStepMinutes: number;
    slots: string[];
    slotDetails: AvailableSlotItemResponse[];
}

export interface AvailableSlotItemResponse {
    time: string;
    startAt: string;
    endAt: string;
    available: boolean;
    reason: string | null;
}

export interface CheckInAppointmentRequest {
    receptionistId?: number | null;
    dentistId?: number | null;
}
