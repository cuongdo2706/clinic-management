package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateTreatmentRequest {
    String code;

    @NotBlank(message = "Name must not be blank")
    String name;

    String description;

    @NotNull(message = "Price must not be null")
    BigDecimal price;

    String unit;


    Long treatmentCategoryId;

    Long version;
}

