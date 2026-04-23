package cd.beapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record MedicineResponse(
        Long id,
        String code,
        String name,
        String unit,
        String description,
        Boolean isActive,
        BigDecimal price,
        Integer quantity,
        String manufacturer,
        String origin,
        Long version,
        Instant createdAt,
        Instant modifiedAt) {
}
