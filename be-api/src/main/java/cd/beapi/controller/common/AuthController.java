package cd.beapi.controller.common;

import cd.beapi.dto.request.ClientRegisterRequest;
import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.response.PatientResponse;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TokenResponse;
import cd.beapi.enumerate.AuthPortal;
import cd.beapi.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String LEGACY_REFRESH_COOKIE = "refreshToken";
    private static final String CLIENT_REFRESH_COOKIE = "clientRefreshToken";
    private static final String CLINIC_REFRESH_COOKIE = "clinicRefreshToken";

    private final AuthService authService;

    private void createResponseCookie(TokenResponse tokenResponse,
                                      AuthPortal portal,
                                      HttpServletResponse response) {
        ResponseCookie refreshCookie = ResponseCookie
                .from(refreshCookieName(portal), tokenResponse.refreshToken())
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(tokenResponse.refreshExpiresIn())
                .build();
        ResponseCookie sessionFlag = ResponseCookie.from("hasSession", "true")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(tokenResponse.refreshExpiresIn())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionFlag.toString());
    }

    @PostMapping("/client/login")
    public SuccessResponse<TokenResponse> clientLogin(@Valid @RequestBody LoginRequest loginRequest,
                                                      HttpServletResponse response) {
        TokenResponse tokenResponse = authService.loginClient(loginRequest);
        return createLoginResponse(tokenResponse, AuthPortal.CLIENT, response);
    }

    @PostMapping("/register")
    public SuccessResponse<PatientResponse> registerClient(@Valid @RequestBody ClientRegisterRequest request) {
        return new SuccessResponse<>(
                HttpStatus.CREATED.value(),
                "Register successfully",
                Instant.now(),
                authService.registerClient(request)
        );
    }

    @PostMapping("/clinic/login")
    public SuccessResponse<TokenResponse> clinicLogin(@Valid @RequestBody LoginRequest loginRequest,
                                                      HttpServletResponse response) {
        TokenResponse tokenResponse = authService.loginClinic(loginRequest);
        return createLoginResponse(tokenResponse, AuthPortal.CLINIC, response);
    }

    private SuccessResponse<TokenResponse> createLoginResponse(TokenResponse tokenResponse,
                                                               AuthPortal portal,
                                                               HttpServletResponse response) {
        createResponseCookie(tokenResponse, portal, response);
        TokenResponse safeResponse = new TokenResponse(
                tokenResponse.accessToken(),
                null,
                tokenResponse.tokenType(),
                tokenResponse.accessExpiresIn(),
                null
        );
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Login successfully",
                Instant.now(),
                safeResponse
        );
    }

    @PostMapping("/logout")
    public SuccessResponse<?> logout(@RequestHeader("Authorization") String authHeader,
                                     @AuthenticationPrincipal Jwt jwt,
                                     HttpServletResponse response) {
        String token = authHeader.substring(7);
        authService.logout(token, jwt.getSubject());
        AuthPortal portal = extractPortal(jwt);
        ResponseCookie clearFlag = ResponseCookie.from("hasSession", "")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie(refreshCookieName(portal)).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefreshCookie(LEGACY_REFRESH_COOKIE).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearFlag.toString());
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Logout successfully",
                Instant.now(),
                null
        );
    }

    @PostMapping("/refresh")
    public SuccessResponse<TokenResponse> refreshToken(@CookieValue(name = "refreshToken", required = false) String refreshTokenRequest,
                                                       HttpServletResponse response) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);
        createResponseCookie(tokenResponse, extractPortalFromToken(tokenResponse), response);
        return createRefreshResponse(tokenResponse);
    }

    @PostMapping("/client/refresh")
    public SuccessResponse<TokenResponse> refreshClientToken(
            @CookieValue(name = CLIENT_REFRESH_COOKIE, required = false) String refreshTokenRequest,
            HttpServletResponse response) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest, AuthPortal.CLIENT);
        createResponseCookie(tokenResponse, AuthPortal.CLIENT, response);
        return createRefreshResponse(tokenResponse);
    }

    @PostMapping("/clinic/refresh")
    public SuccessResponse<TokenResponse> refreshClinicToken(
            @CookieValue(name = CLINIC_REFRESH_COOKIE, required = false) String refreshTokenRequest,
            HttpServletResponse response) {
        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest, AuthPortal.CLINIC);
        createResponseCookie(tokenResponse, AuthPortal.CLINIC, response);
        return createRefreshResponse(tokenResponse);
    }

    private SuccessResponse<TokenResponse> createRefreshResponse(TokenResponse tokenResponse) {
        TokenResponse safeResponse = new TokenResponse(
                tokenResponse.accessToken(),
                null,
                tokenResponse.tokenType(),
                tokenResponse.accessExpiresIn(),
                null
        );
        return new SuccessResponse<>(
                HttpStatus.OK.value(),
                "Get data successfully",
                Instant.now(),
                safeResponse
        );
    }

    private String refreshCookieName(AuthPortal portal) {
        return portal == AuthPortal.CLINIC ? CLINIC_REFRESH_COOKIE : CLIENT_REFRESH_COOKIE;
    }

    private ResponseCookie clearRefreshCookie(String cookieName) {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
    }

    private AuthPortal extractPortal(Jwt jwt) {
        String portal = jwt == null ? null : jwt.getClaimAsString("portal");
        if (portal == null) {
            return AuthPortal.CLIENT;
        }
        return AuthPortal.valueOf(portal);
    }

    private AuthPortal extractPortalFromToken(TokenResponse tokenResponse) {
        if (tokenResponse.accessToken() == null) {
            return AuthPortal.CLIENT;
        }
        String[] chunks = tokenResponse.accessToken().split("\\.");
        if (chunks.length < 2) {
            return AuthPortal.CLIENT;
        }
        String payload = new String(java.util.Base64.getUrlDecoder().decode(chunks[1]));
        return payload.contains("\"portal\":\"CLINIC\"") ? AuthPortal.CLINIC : AuthPortal.CLIENT;
    }
}
