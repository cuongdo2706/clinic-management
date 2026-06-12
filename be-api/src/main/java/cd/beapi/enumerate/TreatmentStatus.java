package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum TreatmentStatus {
    IN_PROGRESS("Đang điều trị"),
    COMPLETED("Hoàn thành"),
    CANCELLED("Đã hủy");

    String label;

    TreatmentStatus(String label) {
        this.label = label;
    }
}
