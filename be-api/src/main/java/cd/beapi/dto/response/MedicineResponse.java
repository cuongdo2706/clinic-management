package cd.beapi.dto.response;

import java.time.Instant;

public record MedicineResponse(
        Long id,
        String code,
        String unit,
        String description,
        Boolean isActive,
        Instant createdAt,
        Instant modifiedAt) {
}
