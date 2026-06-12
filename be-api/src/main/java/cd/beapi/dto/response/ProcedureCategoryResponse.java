package cd.beapi.dto.response;

import java.time.Instant;

public record ProcedureCategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        Long version,
        Instant createdAt,
        Instant modifiedAt) {
}
