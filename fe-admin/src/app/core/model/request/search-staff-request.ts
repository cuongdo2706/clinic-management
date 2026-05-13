import {PaginationFormat} from "./pagination-format";

export interface SearchStaffRequest extends PaginationFormat {
    sortBy: string;
    codeKeyword: string;
    nameKeyword: string;
    phoneKeyword: string;
    staffType: string | null;
    isActive: boolean | null;
}
