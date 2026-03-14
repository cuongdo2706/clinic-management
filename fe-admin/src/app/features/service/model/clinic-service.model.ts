export interface ClinicServiceResponse {
    id: string;
    name: string;
    description: string;
    price: number;
    duration: number; // minutes
    active: boolean;
}

export interface ClinicServiceRequest {
    name: string;
    description: string;
    price: number;
    duration: number;
}

