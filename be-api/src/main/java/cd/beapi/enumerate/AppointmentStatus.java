package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AppointmentStatus {
    PENDING("Chờ xác nhận"),
    CONFIRMED("Đã xác nhận"),
    IN_PROGRESS("Đang khám"),
    COMPLETE("Hoàn thành"),
    CANCELLED("Đã hủy"),
    NO_SHOW("Không đến");

    String label;

    AppointmentStatus(String label) {
        this.label = label;
    }
}
