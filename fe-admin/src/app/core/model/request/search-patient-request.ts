export interface SearchPatientRequest{
    page:number;
    size:number;
    sortBy:string;
    codeKeyword:string;
    nameKeyword:string;
    phoneKeyword:string;
    guardianNameKeyword:string;
    guardianPhoneKeyword:string;
}