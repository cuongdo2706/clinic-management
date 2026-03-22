package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreatePaymentRequest(
        @NotNull(message = "Invoice ID không được để trống")
        Long invoiceId,

        @NotNull(message = "Số tiền không được để trống")
        BigDecimal amount,

        @NotNull(message = "Phương thức thanh toán không được để trống")
        String paymentMethod,    // CASH, CARD, TRANSFER, MOMO, ...

        Long cashierId,          // Staff thu tiền (optional — lấy từ token nếu null)
        String note
) {}

