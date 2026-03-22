package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum VisitRegistrationStatus {
    WAITING("Chờ khám"),
    IN_PROGRESS("Đang khám"),
    DONE("Hoàn thành"),
    CANCELLED("Đã hủy");

    String label;

    VisitRegistrationStatus(String label) {
        this.label = label;
    }
}
