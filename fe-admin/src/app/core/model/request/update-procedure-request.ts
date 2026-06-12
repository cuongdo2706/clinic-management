export interface UpdateProcedureRequest {
    code: string;
    name: string;
    description: string;
    price: number;
    unit: string;
    durationMinutes: number;
    isActive: boolean;
    procedureCategoryId: number | null;
    version: number;
}

