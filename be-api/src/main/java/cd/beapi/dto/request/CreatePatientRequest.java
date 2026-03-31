package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreatePatientRequest {
    String code;

    @NotBlank(message = "Name must have more than 1 digit")
    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String identityNumber;

    String email;

    String address;

    String medicalHistory;

    CreateUserRequest user;
}
