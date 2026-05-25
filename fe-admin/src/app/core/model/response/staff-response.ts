export interface StaffResponse {
    id: number;
    code: string;
    fullName: string;
    phone: string;
    email: string;
    dob: Date;
    gender: boolean | null;
    address: string;
    avatarUrl: string | null;
    staffType: string;
    isActive: boolean;
    version: number;
    createdAt: Date;
    modifiedAt: Date;
}
