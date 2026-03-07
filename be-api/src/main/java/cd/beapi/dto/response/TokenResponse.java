package cd.beapi.dto.response;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long accessExpiresIn,
        Long refreshExpiresIn
) {
}
