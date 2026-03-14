export interface PatientResponse {
    id: string;
    fullName: string;
    phone: string;
    email: string;
    dateOfBirth: string;
    gender: string;
    address: string;
}

export interface PatientRequest {
    fullName: string;
    phone: string;
    email: string;
    dateOfBirth: string;
    gender: string;
    address: string;
}

