package cd.beapi.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateMedicineRequest {
    String code;
    String name;
    String unit;
    String description;
}
