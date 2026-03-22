package cd.beapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreatePrescriptionRequest(
        @NotNull(message = "Medical Record ID không được để trống")
        Long medicalRecordId,

        String note,

        @Valid
        List<PrescriptionItemRequest> items
) {
    public record PrescriptionItemRequest(
            @NotNull(message = "Medicine ID không được để trống")
            Long medicineId,

            @NotNull(message = "Số lượng không được để trống")
            Integer quantity,

            String dosage,       // "3 viên/ngày"
            String instruction   // "Uống sau ăn"
    ) {}
}

