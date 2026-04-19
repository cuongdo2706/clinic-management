export interface PatientResponse{
    id:number;
    code:string;
    fullName:string;
    phone:string;
    dob:Date;
    gender:boolean;
    address:string;
    version:number
    guardianName:string;
    guardianPhone:string;
    createdAt:Date;
    modifiedAt:Date;
}