import {CreateUserRequest} from "./create-user-request";

export interface CreateStaffRequest {
    code: string;
    fullName: string;
    dob: Date;
    gender: boolean;
    phone: string;
    email: string;
    address: string;
    user: CreateUserRequest;
    workingSchedules: WorkingScheduleRequest[];
}

interface WorkingScheduleRequest {
    dayOfWeek: string;
    startTime: Date;
    endTime: Date;
}