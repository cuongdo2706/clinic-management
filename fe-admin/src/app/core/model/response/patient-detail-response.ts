import {AppointmentResponse} from "../../../features/appointment/model/appointment.model";

export type TreatmentStatus = 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';

export interface PatientInfoResponse {
    id: number;
    code: string;
    fullName: string;
    phone: string | null;
    email: string | null;
    gender: boolean | null;
    dob: string | null;
    address: string | null;
    isActive: boolean | null;
    guardianName: string | null;
    guardianPhone: string | null;
}

export interface StaffInfoResponse {
    id: number;
    code: string;
    fullName: string;
    phone: string | null;
    email: string | null;
}

export interface TreatmentProcedureResponse {
    id: number | null;
    code: string | null;
    name: string | null;
    quantity: number | null;
    unitPrice: number | null;
    note: string | null;
}

export interface PrescriptionItemResponse {
    id: number;
    medicineId: number | null;
    medicineCode: string | null;
    medicineName: string | null;
    medicineUnit: string | null;
    quantity: number | null;
    dosage: string | null;
    frequency: string | null;
    duration: string | null;
    instruction: string | null;
    createdAt: string | null;
    modifiedAt: string | null;
}

export interface PrescriptionDetailResponse {
    id: number;
    code: string;
    treatmentId: number | null;
    treatmentDiagnosis: string | null;
    patient: PatientInfoResponse | null;
    doctor: StaffInfoResponse | null;
    prescribedAt: string | null;
    advice: string | null;
    reExaminationDate: string | null;
    note: string | null;
    itemCount: number;
    items: PrescriptionItemResponse[];
    createdAt: string | null;
    modifiedAt: string | null;
}

export interface TreatmentSummaryResponse {
    id: number;
    treatmentDate: string | null;
    status: TreatmentStatus;
    diagnosis: string | null;
    note: string | null;
    patientId: number | null;
    patientName: string | null;
    appointmentId: number | null;
    appointmentCode: string | null;
    doctorId: number | null;
    doctorName: string | null;
    hasPrescription: boolean;
    prescriptionItemCount: number;
    version: number | null;
    createdAt: string | null;
    modifiedAt: string | null;
}

export interface TreatmentDetailResponse {
    id: number;
    treatmentDate: string | null;
    status: TreatmentStatus;
    diagnosis: string | null;
    note: string | null;
    patient: PatientInfoResponse | null;
    doctor: StaffInfoResponse | null;
    appointment: AppointmentResponse | null;
    procedures: TreatmentProcedureResponse[];
    prescription: PrescriptionDetailResponse | null;
    version: number | null;
    createdAt: string | null;
    modifiedAt: string | null;
}

export interface PatientDetailResponse {
    patient: PatientInfoResponse;
    treatments: TreatmentSummaryResponse[];
    prescriptions: PrescriptionDetailResponse[];
    appointments: AppointmentResponse[];
}
