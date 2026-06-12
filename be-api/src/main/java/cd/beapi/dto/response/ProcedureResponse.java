package cd.beapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProcedureResponse(
        Long id,
        String code,
        String name,
        String description,
        BigDecimal price,
        String unit,
        Integer durationMinutes,
        Boolean isActive,
        Long version,
        ProcedureCategoryResponse procedureCategory,
        Instant createdAt,
        Instant modifiedAt) {
}
