package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateInvoiceRequest(
        @NotNull(message = "Appointment ID không được để trống")
        Long appointmentId,

        BigDecimal discountAmount   // Giảm giá (optional, default = 0)
) {}

