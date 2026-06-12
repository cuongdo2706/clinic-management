package cd.beapi.dto.response;

import java.time.LocalDateTime;

public record AvailableSlotItemResponse(
        String time,
        LocalDateTime startAt,
        LocalDateTime endAt,
        boolean available,
        String reason
) {
}
