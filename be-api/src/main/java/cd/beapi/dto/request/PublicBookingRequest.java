package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicBookingRequest {
    @NotBlank(message = "Patient name must not be blank")
    String fullName;

    @NotNull(message = "Date of birth must not be null")
    @PastOrPresent(message = "Date of birth must be in the past")
    LocalDate dob;

    Boolean gender;

    @NotBlank(message = "Phone must not be blank")
    String phone;

    String address;

    String guardianName;

    String guardianPhone;

    @NotNull(message = "Dentist must not be null")
    Long dentistId;

    Long procedureId;

    @NotNull(message = "Appointment date must not be null")
    LocalDateTime appointmentDate;

    @Positive(message = "Duration must be greater than 0")
    Integer durationMinutes;

    String symptom;

    String note;
}
