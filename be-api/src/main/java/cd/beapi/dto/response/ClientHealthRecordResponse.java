package cd.beapi.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public record ClientHealthRecordResponse(
        Long id,
        String code,
        String chiefComplaint,
        String diagnosis,
        String treatmentPlan,
        String notes,
        Integer queueNumber,
        Instant createdAt,
        LocalDateTime appointmentDate,
        String dentistName,
        List<ClientHealthRecordProcedureResponse> procedures,
        ClientPrescriptionResponse prescription
) {
}
