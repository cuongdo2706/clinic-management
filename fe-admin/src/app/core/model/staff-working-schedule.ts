export type DayOfWeekValue =
    'MONDAY'
    | 'TUESDAY'
    | 'WEDNESDAY'
    | 'THURSDAY'
    | 'FRIDAY'
    | 'SATURDAY'
    | 'SUNDAY';

export interface StaffWorkingSchedulePayload {
    dayOfWeek: DayOfWeekValue;
    working: boolean;
    startTime: string;
    endTime: string;
}

export interface StaffWorkingScheduleResponse extends StaffWorkingSchedulePayload {
}
