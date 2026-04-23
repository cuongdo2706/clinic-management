package cd.beapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMedicineRequest {
    String code;

    @NotBlank(message = "Name must not be blank")
    String name;

    String unit;

    String description;

    @Min(value = 0, message = "Price must be >= 0")
    BigDecimal price;

    @Min(value = 0, message = "Quantity must be >= 0")
    Integer quantity;

    String manufacturer;

    String origin;
}
