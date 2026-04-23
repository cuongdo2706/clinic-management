import {TreatmentCategoryResponse} from "./treatment-category-response";

export interface TreatmentResponse{
    id:number;
    code:string;
    name:string;
    description:string;
    price:number;
    unit:string;
    isActive:boolean;
    version:number;
    treatmentCategory:TreatmentCategoryResponse;
    createdAt:Date;
    modifiedAt:Date;
}