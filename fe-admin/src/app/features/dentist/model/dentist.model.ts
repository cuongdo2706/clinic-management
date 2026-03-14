export interface DentistResponse {
    id: string;
    fullName: string;
    phone: string;
    email: string;
    specialty: string;
    active: boolean;
}

export interface DentistRequest {
    fullName: string;
    phone: string;
    email: string;
    specialty: string;
}

