import {ProcedureCategoryResponse} from "./procedure-category-response";

export interface ProcedureResponse{
    id:number;
    code:string;
    name:string;
    description:string;
    price:number;
    unit:string;
    durationMinutes:number;
    isActive:boolean;
    version:number;
    procedureCategory:ProcedureCategoryResponse;
    createdAt:Date;
    modifiedAt:Date;
}
