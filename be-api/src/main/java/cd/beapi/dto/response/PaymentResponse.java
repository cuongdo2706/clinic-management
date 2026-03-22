package cd.beapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
        Long id,
        String code,
        BigDecimal amount,
        String paymentMethod,
        String paymentMethodLabel,
        String note,
        String cashierName,
        String invoiceCode,
        String invoiceStatus,
        String invoiceStatusLabel,
        BigDecimal invoiceRemaining,
        Instant paidAt,
        Instant createdAt
) {}

