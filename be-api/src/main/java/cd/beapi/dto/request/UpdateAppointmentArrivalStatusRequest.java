package cd.beapi.dto.request;

import cd.beapi.enumerate.AppointmentArrivalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateAppointmentArrivalStatusRequest {
    @NotNull(message = "Appointment arrival status must not be null")
    AppointmentArrivalStatus arrivalStatus;
}
