import {PaginationFormat} from "./pagination-format";

export interface SearchMedicineRequest extends PaginationFormat{
    sortBy:string;
    codeKeyword:string;
    nameKeyword:string;
    priceFrom:number|null;
    priceTo:number|null;
}