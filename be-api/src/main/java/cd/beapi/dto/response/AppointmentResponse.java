package cd.beapi.dto.response;

import cd.beapi.enumerate.AppointmentStatus;

import java.time.Instant;

public record AppointmentResponse(
        Long id,
        String code,
        String appointmentDate,
        Instant createdAt,
        Instant modifiedAt,
        String symptom,
        String note,
        AppointmentStatus status,
        Long version,
        PatientResponse patient
) {}

