import {RoleResponse} from "./role-response";

export interface PagePermission {
    pageCode: string;
    pageName: string;
    granted: { [actionCode: string]: boolean };
}

export interface PermissionResponse {
    role: RoleResponse;
    actions: string[];
    pages: PagePermission[];
}