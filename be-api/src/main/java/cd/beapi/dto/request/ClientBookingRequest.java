package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ClientBookingRequest {
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
