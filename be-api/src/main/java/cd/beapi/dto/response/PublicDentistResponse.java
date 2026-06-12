package cd.beapi.dto.response;

public record PublicDentistResponse(
        Long id,
        String code,
        String fullName,
        String avatarUrl
) {
}
