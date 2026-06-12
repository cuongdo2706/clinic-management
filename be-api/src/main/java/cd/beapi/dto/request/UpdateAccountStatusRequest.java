package cd.beapi.dto.request;

import cd.beapi.enumerate.AccountStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAccountStatusRequest {
    @NotNull
    AccountStatus status;
}
