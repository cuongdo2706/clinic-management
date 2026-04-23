export interface MedicineResponse {
    id: number;
    code: string;
    name: string;
    unit: string;
    price:number;
    quantity:number;
    manufacturer:string;
    origin:string;
    description: string;
    isActive: boolean;
    version:number
    createdAt: Date;
    modifiedAt: Date;
}