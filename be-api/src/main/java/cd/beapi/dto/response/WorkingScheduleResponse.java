package cd.beapi.dto.response;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;

public record WorkingScheduleResponse(
        Long id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        Instant createdAt,
        Instant modifiedAt) {
}
