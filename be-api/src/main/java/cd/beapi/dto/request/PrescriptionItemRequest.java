package cd.beapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionItemRequest {
    @NotNull(message = "Medicine must not be null")
    Long medicineId;

    String dosage;

    String frequency;

    String duration;

    @Min(value = 1, message = "Quantity must be greater than 0")
    Integer quantity;

    String instruction;
}
