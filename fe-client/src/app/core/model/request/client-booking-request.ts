export interface ClientBookingRequest {
  dentistId: number;
  procedureId?: number | null;
  appointmentDate: string;
  durationMinutes: number;
  symptom: string;
}
