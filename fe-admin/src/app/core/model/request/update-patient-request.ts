export interface UpdatePatientRequest{
    code:string;
    fullName:string;
    dob:Date;
    gender:boolean;
    phone:string;
    address:string;
    guardianName:string;
    guardianPhone:string;
    version:number;
}