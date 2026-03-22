package cd.beapi.dto.response;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        String code,
        String appointmentDate,
        String startTime,
        String endTime,
        String status,
        String statusLabel,
        String note,
        String patientName,
        String patientCode,
        String dentistName,
        String dentistCode,
        Instant createdAt
) {}

