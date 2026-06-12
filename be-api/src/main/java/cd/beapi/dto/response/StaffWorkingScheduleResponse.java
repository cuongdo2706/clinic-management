package cd.beapi.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record StaffWorkingScheduleResponse(
        DayOfWeek dayOfWeek,
        Boolean working,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime
) {
}
