package cd.beapi.dto.response;

import java.time.Instant;

public record TreatmentCategoryResponse(
        Long id,
        String code,
        String name,
        String description,
        Instant createdAt,
        Instant modifiedAt) {
}
