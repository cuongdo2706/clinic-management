package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum InvoiceStatus {
    UNPAID("Chưa thanh toán"),
    PAID("Đã thanh toán"),
    PARTIALLY_PAID("Thanh toán một phần"),
    CANCELLED("Đã hủy");

    String label;

    InvoiceStatus(String label) {
        this.label = label;
    }
}
