import {StaffWorkingSchedulePayload} from '../staff-working-schedule';

export interface UpdateStaffRequest {
    code: string;
    fullName: string;
    dob: string | null;
    gender: boolean | null;
    phone: string;
    email: string;
    address: string;
    staffType: string;
    isActive: boolean;
    version: number;
    workingSchedules: StaffWorkingSchedulePayload[];
}
