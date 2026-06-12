package cd.beapi.dto.response;

import java.math.BigDecimal;

public record TreatmentProcedureResponse(
        Long id,
        String code,
        String name,
        Integer quantity,
        BigDecimal unitPrice,
        String note
) {
}
