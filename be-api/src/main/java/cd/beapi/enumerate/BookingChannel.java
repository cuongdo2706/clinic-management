package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum BookingChannel {
    ONLINE_AUTHENTICATED("Đặt lịch online (có tài khoản)"),
    ONLINE_GUEST("Đặt lịch online (khách vãng lai)"),
    WALK_IN("Đến trực tiếp"),
    PHONE_CALL("Đặt lịch qua điện thoại");

    String label;

    BookingChannel(String label) {
        this.label = label;
    }
}

