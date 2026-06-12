export type AccountType = 'STAFF' | 'CUSTOMER';
export type AccountStatus = 'ACTIVE' | 'LOCKED' | 'DISABLED';

export interface AccountResponse {
    id: number;
    type: AccountType;
    username: string;
    ownerId: number;
    ownerName: string;
    ownerCode: string;
    phone: string | null;
    email: string | null;
    roleCode: string | null;
    roleName: string | null;
    status: AccountStatus;
    mustChangePassword: boolean;
    lastLoginAt: string | null;
    createdAt: string;
}

export interface SearchAccountRequest {
    page: number;
    size: number;
    type: AccountType;
    keyword: string;
    roleCode: string | null;
    status: AccountStatus | null;
}

export interface ResetPasswordResponse {
    temporaryPassword: string;
}
