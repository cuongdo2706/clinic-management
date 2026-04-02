package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdatePatientRequest {
    String code;

    @NotBlank(message = "Name must have more than 1 digit")
    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String address;

    String guardianName;

    String guardianPhone;
}
