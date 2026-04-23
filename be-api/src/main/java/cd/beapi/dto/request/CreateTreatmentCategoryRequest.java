package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateTreatmentCategoryRequest {
    String code;

    @NotBlank(message = "Name must not be blank")
    String name;

    String description;
}

