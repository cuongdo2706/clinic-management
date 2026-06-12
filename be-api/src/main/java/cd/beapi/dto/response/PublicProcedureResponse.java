package cd.beapi.dto.response;

import java.math.BigDecimal;

public record PublicProcedureResponse(
        Long id,
        String code,
        String name,
        BigDecimal price,
        String unit,
        Integer durationMinutes
) {
}
