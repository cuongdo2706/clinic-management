export interface WorkingScheduleResponse {
    id: number;
    dayOfWeek: string;
    startTime: string;
    endTime: string;
    createdAt: Date;
    modifiedAt: Date;
}

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
    workingSchedules: WorkingScheduleResponse[];
    version: number;
    createdAt: Date;
    modifiedAt: Date;
}
