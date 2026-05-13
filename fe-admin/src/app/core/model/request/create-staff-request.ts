export interface CreateStaffRequest {
    code: string;
    fullName: string;
    dob: string | null;
    gender: boolean | null;
    phone: string;
    email: string;
    address: string;
    staffType: string;
    isActive: boolean;
    roleCode: string | null;
    workingSchedules: WorkingScheduleRequest[];
}

export interface WorkingScheduleRequest {
    dayOfWeek: string;
    startTime: string;
    endTime: string;
}
