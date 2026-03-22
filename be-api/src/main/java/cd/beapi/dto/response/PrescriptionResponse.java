package cd.beapi.dto.response;

import java.time.Instant;
import java.util.List;

public record PrescriptionResponse(
        Long id,
        String code,
        String note,
        String patientName,
        String dentistName,
        String medicalRecordCode,
        List<PrescriptionItemDetail> items,
        Instant createdAt
) {
    public record PrescriptionItemDetail(
            Long id,
            String medicineName,
            String medicineUnit,
            Integer quantity,
            String dosage,
            String instruction
    ) {}
}

