package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum DentistStatus {
    AVAILABLE("Sẵn sàng"),
    IN_SESSION("Đang khám"),
    ON_BREAK("Nghỉ giữa ca"),
    OFF_DUTY("Hết ca");

    String label;

    DentistStatus(String label) {
        this.label = label;
    }
}
