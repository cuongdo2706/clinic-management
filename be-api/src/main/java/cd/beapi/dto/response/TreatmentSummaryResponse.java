package cd.beapi.dto.response;

import cd.beapi.enumerate.TreatmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record TreatmentSummaryResponse(
        Long id,
        LocalDateTime treatmentDate,
        TreatmentStatus status,
        String diagnosis,
        String note,
        Long patientId,
        String patientName,
        Long appointmentId,
        String appointmentCode,
        Long doctorId,
        String doctorName,
        Boolean hasPrescription,
        Integer prescriptionItemCount,
        Long version,
        Instant createdAt,
        Instant modifiedAt
) {
}
