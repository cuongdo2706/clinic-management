import {PaginationFormat} from "./pagination-format";

export interface SearchStaffRequest extends PaginationFormat {
    sortBy: string;
    codeKeyword: string;
    nameKeyword: string;
    phoneKeyword: string;
    emailKeyword: string;
    staffType: string | null;
}
