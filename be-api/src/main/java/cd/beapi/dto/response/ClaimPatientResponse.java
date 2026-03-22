package cd.beapi.dto.response;

/**
 * Response sau khi đăng ký + claim Patient thành công.
 */
public record ClaimPatientResponse(
        Long userId,
        Long patientId,
        String patientCode,
        String fullName,
        String phone,
        boolean claimed  // true = đã link vào Patient cũ, false = tạo Patient mới
) {}

