export interface UpdateTreatmentRequest {
    code: string;
    name: string;
    description: string;
    price: number;
    unit: string;
    isActive: boolean;
    treatmentCategoryId: number | null;
    version: number;
}

