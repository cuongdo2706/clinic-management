package cd.beapi.dto.response;

import cd.beapi.enumerate.AccountStatus;
import cd.beapi.enumerate.AccountType;

import java.time.Instant;

public record AccountResponse(
        Long id,
        AccountType type,
        String username,
        Long ownerId,
        String ownerName,
        String ownerCode,
        String phone,
        String email,
        String roleCode,
        String roleName,
        AccountStatus status,
        Boolean mustChangePassword,
        Instant lastLoginAt,
        Instant createdAt
) {
}
