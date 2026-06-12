package cd.beapi.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableSlotResponse(
        Long dentistId,
        LocalDate date,
        Integer durationMinutes,
        Integer slotStepMinutes,
        List<String> slots,
        List<AvailableSlotItemResponse> slotDetails
) {
}
