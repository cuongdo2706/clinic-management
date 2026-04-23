import {PaginationFormat} from "./pagination-format";

export interface SearchTreatmentRequest extends PaginationFormat {
    sortBy: string;
    codeKeyword: string;
    nameKeyword: string;
    priceFrom: number | null;
    priceTo: number | null;
}

