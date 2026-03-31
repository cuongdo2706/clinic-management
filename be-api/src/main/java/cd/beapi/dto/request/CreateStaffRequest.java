package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public class CreateStaffRequest {
    String code;

    @NotBlank(message = "Name must have more than 1 digit")
    String fullName;

    LocalDate dob;

    Boolean gender;

    String phone;

    String email;

    String address;

    CreateUserRequest user;
}
