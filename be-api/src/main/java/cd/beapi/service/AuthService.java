package cd.beapi.service;

import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.response.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest loginRequest);
    void logout(String accessToken, String username);
    TokenResponse refreshToken(String refreshTokenRequest);
}
