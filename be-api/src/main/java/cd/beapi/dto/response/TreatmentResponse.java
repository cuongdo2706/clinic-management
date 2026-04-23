package cd.beapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record TreatmentResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        String unit,
        Boolean isActive,
        Long version,
        TreatmentCategoryResponse treatmentCategory,
        Instant createdAt,
        Instant modifiedAt) {
}
