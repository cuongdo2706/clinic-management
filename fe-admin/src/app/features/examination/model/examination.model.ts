// ══════════════════════════════════════
//  REQUEST interfaces (match backend DTOs)
// ══════════════════════════════════════

export interface WalkInPatientRequest {
    fullName: string;
    phone: string;
    dob?: string;
    gender?: boolean;
    email?: string;
    identityNumber?: string;
    address?: string;
}

export interface CreateAppointmentRequest {
    patientId: number;
    dentistId: number;
    appointmentDate: string;   // yyyy-MM-dd
    startTime: string;         // HH:mm
    endTime: string;           // HH:mm
    note?: string;
}

export interface CheckInRequest {
    appointmentId: number;
    receptionistId?: number;
    note?: string;
}

export interface CreateMedicalRecordRequest {
    appointmentId: number;
    chiefComplaint?: string;
    diagnosis?: string;
    treatmentPlan?: string;
    notes?: string;
    serviceIds?: number[];
}

export interface CreatePrescriptionRequest {
    medicalRecordId: number;
    note?: string;
    items: PrescriptionItemRequest[];
}

export interface PrescriptionItemRequest {
    medicineId: number;
    quantity: number;
    dosage?: string;
    instruction?: string;
}

export interface CreateInvoiceRequest {
    appointmentId: number;
    discountAmount?: number;
}

export interface CreatePaymentRequest {
    invoiceId: number;
    amount: number;
    paymentMethod: string;
    cashierId?: number;
    note?: string;
}

// ══════════════════════════════════════
//  RESPONSE interfaces
// ══════════════════════════════════════

export interface PatientResponse {
    id: number;
    code: string;
    fullName: string;
    phone: string;
    dob?: string;
    gender?: boolean;
    email?: string;
    address?: string;
    isWalkIn?: boolean;
    createdAt?: string;
}

export interface AppointmentResponse {
    id: number;
    code: string;
    appointmentDate: string;
    startTime: string;
    endTime: string;
    status: string;
    statusLabel: string;
    note?: string;
    patientName: string;
    patientCode: string;
    dentistName: string;
    dentistCode: string;
    createdAt?: string;
}

export interface CheckInResponse {
    id: number;
    code: string;
    queueNumber: number;
    status: string;
    statusLabel: string;
    note?: string;
    appointmentCode: string;
    patientName: string;
    receptionistName?: string;
    createdAt?: string;
}

export interface MedicalRecordResponse {
    id: number;
    code: string;
    chiefComplaint?: string;
    diagnosis?: string;
    treatmentPlan?: string;
    notes?: string;
    patientName: string;
    patientCode: string;
    dentistName: string;
    appointmentCode: string;
    services: ServiceItem[];
    createdAt?: string;
}

export interface ServiceItem {
    id: number;
    code: string;
    name: string;
    price: string;
}

export interface PrescriptionResponse {
    id: number;
    code: string;
    note?: string;
    patientName: string;
    dentistName: string;
    medicalRecordCode: string;
    items: PrescriptionItemDetail[];
    createdAt?: string;
}

export interface PrescriptionItemDetail {
    id: number;
    medicineName: string;
    medicineUnit: string;
    quantity: number;
    dosage?: string;
    instruction?: string;
}

export interface InvoiceResponse {
    id: number;
    code: string;
    status: string;
    statusLabel: string;
    patientName: string;
    appointmentCode: string;
    totalAmount: number;
    discountAmount: number;
    finalAmount: number;
    items: InvoiceItemDetail[];
    createdAt?: string;
}

export interface InvoiceItemDetail {
    id: number;
    type: string;       // "SERVICE" | "MEDICINE"
    name: string;
    quantity: number;
    unitPrice: number;
    amount: number;
    description: string;
}

export interface PaymentResponse {
    id: number;
    code: string;
    amount: number;
    paymentMethod: string;
    paymentMethodLabel: string;
    note?: string;
    cashierName?: string;
    invoiceCode: string;
    invoiceStatus: string;
    invoiceStatusLabel: string;
    invoiceRemaining: number;
    paidAt?: string;
    createdAt?: string;
}

// ══════════════════════════════════════
//  FORM helper interfaces
// ══════════════════════════════════════

export interface PrescriptionItemForm {
    medicineId: number | null;
    quantity: number;
    dosage: string;
    instruction: string;
}

