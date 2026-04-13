export interface MedicineResponse {
    id: number;
    code: string;
    name: string;
    unit: string;
    description: string;
    isActive: boolean;
    version:number
    createdAt: Date;
    modifiedAt: Date;
}