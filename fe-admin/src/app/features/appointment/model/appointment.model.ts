export interface AppointmentResponse {
    id: string;
    patientName: string;
    dentistName: string;
    appointmentDate: string;
    timeSlot: string;
    status: string;
    notes: string;
}

export interface AppointmentRequest {
    patientId: string;
    dentistId: string;
    appointmentDate: string;
    timeSlot: string;
    notes: string;
}

