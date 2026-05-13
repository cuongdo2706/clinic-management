package cd.beapi.dto.response;

import cd.beapi.enumerate.AppointmentStatus;

import java.time.Instant;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        String code,
        LocalDateTime appointmentDate,
        Instant createdAt,
        Instant modifiedAt,
        String symptom,
        String note,
        AppointmentStatus status,
        Integer queueNumber,
        Long version,
        Long patientId,
        String patientCode,
        String patientName,
        String patientPhone,
        Long dentistId,
        String dentistCode,
        String dentistName,
        Long receptionistId,
        String receptionistName,
        String snapshotPatientName,
        String snapshotPatientPhone
) {}
