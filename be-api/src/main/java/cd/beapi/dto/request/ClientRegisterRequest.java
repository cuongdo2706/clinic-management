package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientRegisterRequest {
    @NotBlank(message = "Username must not be blank")
    String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 6, message = "Password must have at least 6 characters")
    String password;

    @NotBlank(message = "Name must not be blank")
    String fullName;

    @NotNull(message = "Date of birth must not be null")
    @PastOrPresent(message = "Date of birth must be in the past")
    LocalDate dob;

    Boolean gender;

    String phone;

    String address;

    String guardianName;

    String guardianPhone;
}
