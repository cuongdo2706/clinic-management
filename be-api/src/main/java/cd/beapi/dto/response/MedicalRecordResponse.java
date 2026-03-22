package cd.beapi.dto.response;

import java.time.Instant;
import java.util.List;

public record MedicalRecordResponse(
        Long id,
        String code,
        String chiefComplaint,
        String diagnosis,
        String treatmentPlan,
        String notes,
        String patientName,
        String patientCode,
        String dentistName,
        String appointmentCode,
        List<ServiceItem> services,
        Instant createdAt
) {
    public record ServiceItem(
            Long id,
            String code,
            String name,
            String price
    ) {}
}

