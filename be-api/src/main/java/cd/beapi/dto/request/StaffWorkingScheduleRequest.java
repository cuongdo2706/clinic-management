package cd.beapi.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class StaffWorkingScheduleRequest {
    DayOfWeek dayOfWeek;

    Boolean working;

    LocalTime startTime;

    LocalTime endTime;
}
