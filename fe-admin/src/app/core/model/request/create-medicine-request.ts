export interface CreateMedicineRequest{
    code:string;
    name:string;
    unit:string;
    price:number;
    quantity:number;
    manufacturer:string;
    origin:string;
    description:string;
}