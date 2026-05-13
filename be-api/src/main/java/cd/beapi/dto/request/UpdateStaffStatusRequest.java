package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateStaffStatusRequest {
    @NotNull(message = "Active status is required")
    Boolean isActive;
    Long version;
}
