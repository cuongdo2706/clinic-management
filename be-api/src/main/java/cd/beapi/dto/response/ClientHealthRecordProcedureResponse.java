package cd.beapi.dto.response;

import java.math.BigDecimal;

public record ClientHealthRecordProcedureResponse(
        Long procedureId,
        String procedureName,
        Integer quantity,
        BigDecimal unitPrice,
        String note
) {
}
