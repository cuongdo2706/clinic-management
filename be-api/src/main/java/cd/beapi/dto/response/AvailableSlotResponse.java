package cd.beapi.dto.response;

import java.time.LocalDate;
import java.util.List;

public record AvailableSlotResponse(
        Long dentistId,
        LocalDate date,
        List<String> slots
) {
}
