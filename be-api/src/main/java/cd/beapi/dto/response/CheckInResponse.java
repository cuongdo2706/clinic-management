package cd.beapi.dto.response;

import java.time.Instant;

public record CheckInResponse(
        Long id,
        String code,
        Integer queueNumber,
        String status,
        String statusLabel,
        String note,
        String appointmentCode,
        String patientName,
        String receptionistName,
        Instant createdAt
) {}

