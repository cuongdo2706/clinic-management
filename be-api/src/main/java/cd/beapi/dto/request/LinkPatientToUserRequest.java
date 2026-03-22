package cd.beapi.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request để Admin liên kết thủ công Patient walk-in với User đã tồn tại.
 * Dùng khi BN đổi SĐT hoặc không thể tự claim.
 */
public record LinkPatientToUserRequest(
        @NotNull(message = "Patient ID không được để trống")
        Long patientId,

        @NotNull(message = "User ID không được để trống")
        Long userId
) {}

