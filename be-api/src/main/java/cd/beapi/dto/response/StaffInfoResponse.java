package cd.beapi.dto.response;

public record StaffInfoResponse(
        Long id,
        String code,
        String fullName,
        String phone,
        String email
) {
}
