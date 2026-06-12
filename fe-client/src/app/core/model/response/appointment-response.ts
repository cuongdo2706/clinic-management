export type AppointmentStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'IN_PROGRESS'
  | 'COMPLETED'
  | 'CANCELLED';

export type AppointmentArrivalStatus = 'NOT_ARRIVED' | 'ARRIVED' | 'NO_SHOW';

export interface AppointmentResponse {
  id: number;
  code: string;
  appointmentDate: string;
  createdAt: string;
  modifiedAt: string | null;
  estimatedDurationMinutes: number;
  symptom: string | null;
  note: string | null;
  status: AppointmentStatus;
  arrivalStatus: AppointmentArrivalStatus | null;
  queueNumber: number | null;
  version: number;
  patientId: number;
  patientCode: string;
  dentistName: string | null;
  patientName: string;
  patientPhone: string | null;
}
