package cd.beapi.dto.response;

import java.time.Instant;

public record MedicineResponse(
        Long id,
        String code,
        String name,
        String unit,
        Long version,
        String description,
        Boolean isActive,
        Instant createdAt,
        Instant modifiedAt) {
}
