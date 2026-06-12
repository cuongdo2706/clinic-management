import {PaginationFormat} from "./pagination-format";

export interface SearchProcedureRequest extends PaginationFormat {
    sortBy: string;
    codeKeyword: string;
    nameKeyword: string;
    priceFrom: number | null;
    priceTo: number | null;
}

