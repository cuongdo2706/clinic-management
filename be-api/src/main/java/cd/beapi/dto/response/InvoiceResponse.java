package cd.beapi.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String code,
        String status,
        String statusLabel,
        String patientName,
        String appointmentCode,
        BigDecimal totalAmount,
        BigDecimal discountAmount,
        BigDecimal finalAmount,
        List<InvoiceItemDetail> items,
        Instant createdAt
) {
    public record InvoiceItemDetail(
            Long id,
            String type,          // "SERVICE" hoặc "MEDICINE"
            String name,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal amount,
            String description
    ) {}
}

