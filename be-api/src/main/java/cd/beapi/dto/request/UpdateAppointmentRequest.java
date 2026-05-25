package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAppointmentRequest {
    @NotNull(message = "Patient must not be null")
    Long patientId;

    Long dentistId;

    @NotNull(message = "Appointment date must not be null")
    LocalDateTime appointmentDate;

    @NotNull(message = "Estimated duration must not be null")
    @Positive(message = "Estimated duration must be greater than 0")
    Integer estimatedDurationMinutes;

    String symptom;

    String note;

    Long version;
}
