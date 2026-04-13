export interface UpdatePermissionRequest {
    roleId: number;
    permissions: {
        pageCode: string,
        actionCode: string
    }[];
}