package cd.beapi.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PaymentMethod {
    CASH("Tiền mặt"),
    CARD("Thẻ"),
    TRANSFER("Chuyển khoản"),
    MOMO("MoMo"),
    ZALO_PAY("ZaloPay"),
    VNPAY("VNPay");

    String label;

    PaymentMethod(String label) {
        this.label = label;
    }
}

