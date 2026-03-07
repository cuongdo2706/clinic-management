package cd.beapi.controller.common;

import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.response.SuccessResponse;
import cd.beapi.dto.response.TokenResponse;
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
    private final AuthService authService;

    private void createResponseCookie(TokenResponse tokenResponse, HttpServletResponse response) {
        ResponseCookie refreshCookie = ResponseCookie
                .from("refreshToken", tokenResponse.refreshToken())
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

    @PostMapping("/login")
    public SuccessResponse<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest,
                                                HttpServletResponse response) {
        TokenResponse tokenResponse = authService.login(loginRequest);
        createResponseCookie(tokenResponse, response);
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
        ResponseCookie clearCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        ResponseCookie clearFlag = ResponseCookie.from("hasSession", "")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/api/v1/auth")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, clearCookie.toString());
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
        createResponseCookie(tokenResponse, response);
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
}
