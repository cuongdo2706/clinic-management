package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateAppointmentRequest {
    @NotNull(message = "Patient must not be null")
    Long patientId;

    Long dentistId;

    @NotNull(message = "Appointment date must not be null")
    LocalDateTime appointmentDate;

    String symptom;

    String note;
}
