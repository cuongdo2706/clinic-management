import {WorkingScheduleRequest} from "./create-staff-request";

export interface UpdateStaffRequest {
    code: string;
    fullName: string;
    dob: string | null;
    gender: boolean | null;
    phone: string;
    email: string;
    address: string;
    staffType: string;
    workingSchedules: WorkingScheduleRequest[];
    version: number;
}
