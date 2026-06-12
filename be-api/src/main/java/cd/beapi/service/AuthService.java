package cd.beapi.service;

import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.request.ClientRegisterRequest;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.TokenResponse;
import cd.beapi.enumerate.AuthPortal;

public interface AuthService {
    PatientResponse registerClient(ClientRegisterRequest request);
    TokenResponse loginClient(LoginRequest loginRequest);
    TokenResponse loginClinic(LoginRequest loginRequest);
    void logout(String accessToken, String username);
    TokenResponse refreshToken(String refreshTokenRequest);
    TokenResponse refreshToken(String refreshTokenRequest, AuthPortal expectedPortal);
}
