package cd.beapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TreatmentProcedureRequest {
    @NotNull(message = "Procedure must not be null")
    Long procedureId;

    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;

    BigDecimal unitPrice;

    String note;
}
