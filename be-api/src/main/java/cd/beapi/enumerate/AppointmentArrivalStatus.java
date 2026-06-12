package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AppointmentArrivalStatus {
    NOT_ARRIVED("Chưa đến"),
    ARRIVED("Đã đến"),
    NO_SHOW("Không đến");

    String label;

    AppointmentArrivalStatus(String label) {
        this.label = label;
    }
}
