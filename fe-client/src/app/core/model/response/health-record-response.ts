export interface HealthRecordProcedureResponse {
  procedureName: string | null;
  quantity: number;
  unitPrice: number;
  note: string | null;
}

export interface PrescriptionItemResponse {
  medicineName: string | null;
  quantity: number;
  dosage: string | null;
  frequency: string | null;
  duration: string | null;
  instruction: string | null;
}

export interface PrescriptionResponse {
  id: number;
  code: string;
  note: string | null;
  createdAt: string;
  items: PrescriptionItemResponse[];
}

export interface HealthRecordResponse {
  id: number;
  code: string;
  chiefComplaint: string | null;
  diagnosis: string | null;
  treatmentPlan: string | null;
  notes: string | null;
  createdAt: string;
  appointmentDate: string | null;
  dentistName: string | null;
  procedures: HealthRecordProcedureResponse[];
  prescription: PrescriptionResponse | null;
}
