package cd.beapi.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String code,
        String fullName,
        String phone,
        LocalDate dob,
        Boolean gender,
        String address,
        String guardianName,
        String guardianPhone,
        Long version,
        Instant createdAt,
        Instant modifiedAt
) {}

