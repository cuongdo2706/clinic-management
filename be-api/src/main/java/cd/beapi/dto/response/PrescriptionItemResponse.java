package cd.beapi.dto.response;

import java.time.Instant;

public record PrescriptionItemResponse(
        Long id,
        Long medicineId,
        String medicineCode,
        String medicineName,
        String medicineUnit,
        Integer quantity,
        String dosage,
        String frequency,
        String duration,
        String instruction,
        Instant createdAt,
        Instant modifiedAt
) {
}
