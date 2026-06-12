package cd.beapi.dto.request;

import cd.beapi.enumerate.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAppointmentStatusRequest {
    @NotNull(message = "Appointment status must not be null")
    AppointmentStatus status;

    String reason;
}
