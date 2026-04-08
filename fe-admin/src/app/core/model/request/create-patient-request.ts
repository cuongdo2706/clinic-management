export interface CreatePatientRequest{
    code:string;
    fullName:string;
    dob:Date;
    gender:boolean|null;
    phone:string;
    address:string;
    guardianName:string;
    guardianPhone:string;
}