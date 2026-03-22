package cd.beapi.dto.response;

import java.time.Instant;

public record PatientResponse(
        Long id,
        String code,
        String fullName,
        String phone,
        String dob,
        Boolean gender,
        String email,
        String address,
        Boolean isWalkIn,
        Boolean isMinor,
        // Guardian info
        Long guardianId,
        String guardianCode,
        String guardianName,
        String guardianPhone,
        String guardianRelationship,
        String guardianRelationshipLabel,
        Instant createdAt
) {}

