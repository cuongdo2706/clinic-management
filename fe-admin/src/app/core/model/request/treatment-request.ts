import {TreatmentStatus} from "../response/patient-detail-response";

export interface TreatmentProcedureRequest {
    procedureId: number;
    quantity: number | null;
    unitPrice: number | null;
    note: string | null;
}

export interface PrescriptionItemRequest {
    medicineId: number;
    dosage: string | null;
    frequency: string | null;
    duration: string | null;
    quantity: number | null;
    instruction: string | null;
}

export interface PrescriptionRequest {
    doctorId: number | null;
    prescribedAt: string | null;
    advice: string | null;
    reExaminationDate: string | null;
    note: string | null;
    items: PrescriptionItemRequest[] | null;
}

export interface CreateTreatmentRequest {
    patientId: number;
    appointmentId: number | null;
    doctorId: number | null;
    diagnosis: string | null;
    note: string | null;
    treatmentDate: string | null;
    status: TreatmentStatus | null;
    prescription: PrescriptionRequest | null;
    procedures: TreatmentProcedureRequest[] | null;
}

export interface UpdateTreatmentRequest extends Omit<CreateTreatmentRequest, 'patientId'> {
    version: number | null;
}
