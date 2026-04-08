package cd.beapi.dto.response;

import cd.beapi.enumerate.StaffType;

import java.time.Instant;
import java.time.LocalDate;

public record StaffResponse(
        Long id,
        String code,
        String fullName,
        String phone,
        String email,
        LocalDate dob,
        Boolean gender,
        String address,
        String avatarUrl,
        StaffType staffType,
        Instant createdAt,
        Instant modifiedAt) {
}
