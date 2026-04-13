import {PaginationFormat} from "./pagination-format";

export interface SearchPatientRequest extends PaginationFormat{
    sortBy:string;
    codeKeyword:string;
    nameKeyword:string;
    phoneKeyword:string;
    guardianNameKeyword:string;
    guardianPhoneKeyword:string;
}