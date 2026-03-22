package cd.beapi.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request để liên kết (claim) tài khoản User với Patient walk-in đã tồn tại.
 * Dùng khi BN walk-in (đã khám trước đó) đăng ký tài khoản online.
 */
public record ClaimPatientRequest(
        @NotBlank(message = "Số điện thoại không được để trống")
        String phone,

        @NotBlank(message = "Họ tên không được để trống")
        String fullName,

        @NotBlank(message = "Mật khẩu không được để trống")
        String password,

        String email
) {}

