package cd.beapi.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateUserRequest {
    String username;
    String password;
    String roleCode;
}
