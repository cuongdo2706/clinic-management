package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateMedicineRequest {
    String code;

    @NotBlank(message = "Name must not be blank")
    String name;

    String unit;

    String description;

    Long version;
}
