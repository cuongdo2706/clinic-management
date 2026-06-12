package cd.beapi.dto.response;

import cd.beapi.enumerate.TreatmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record TreatmentDetailResponse(
        Long id,
        LocalDateTime treatmentDate,
        TreatmentStatus status,
        String diagnosis,
        String note,
        PatientInfoResponse patient,
        StaffInfoResponse doctor,
        AppointmentResponse appointment,
        List<TreatmentProcedureResponse> procedures,
        PrescriptionDetailResponse prescription,
        Long version,
        Instant createdAt,
        Instant modifiedAt
) {
}
