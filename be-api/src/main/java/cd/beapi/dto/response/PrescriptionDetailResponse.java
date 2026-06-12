package cd.beapi.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PrescriptionDetailResponse(
        Long id,
        String code,
        Long treatmentId,
        String treatmentDiagnosis,
        PatientInfoResponse patient,
        StaffInfoResponse doctor,
        LocalDateTime prescribedAt,
        String advice,
        LocalDate reExaminationDate,
        String note,
        Integer itemCount,
        List<PrescriptionItemResponse> items,
        Instant createdAt,
        Instant modifiedAt
) {
}
