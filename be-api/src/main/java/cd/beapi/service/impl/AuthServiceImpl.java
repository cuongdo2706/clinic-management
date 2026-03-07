package cd.beapi.service.impl;

import cd.beapi.dto.request.LoginRequest;
import cd.beapi.dto.response.TokenResponse;
import cd.beapi.exception.AppException;
import cd.beapi.security.jwt.JwtTokenProvider;
import cd.beapi.security.service.RefreshTokenService;
import cd.beapi.security.service.TokenBlacklistService;
import cd.beapi.service.AuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            //Gen token
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            //Save token into redis
            refreshTokenService.saveRefreshToken(authentication.getName(), jwtTokenProvider.getClaims(refreshToken).getId());
            return new TokenResponse(
                    accessToken,
                    refreshToken,
                    "Bearer",
                    jwtTokenProvider.getAccessExpirationInSeconds(),
                    jwtTokenProvider.getRefreshExpirationInSeconds());
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid username or password");
        } catch (DisabledException e) {
            throw new BadCredentialsException("This account has been disabled");
        }
    }

    @Override
    public void logout(String accessToken, String username) {
        Claims claims = jwtTokenProvider.getClaims(accessToken);
        tokenBlacklistService.blacklistToken(claims.getId(), claims.getExpiration());
        refreshTokenService.deleteRefreshToken(username);
    }

    @Override
    public TokenResponse refreshToken(String refreshTokenRequest) {
        if (refreshTokenRequest == null)
            throw new AppException("Refresh token not found", HttpStatus.BAD_REQUEST);
        Claims claims = jwtTokenProvider.getClaims(refreshTokenRequest);
        if (!"refresh".equals(claims.get("type", String.class)))
            throw new AppException("Invalid token type", HttpStatus.UNAUTHORIZED);
        String username = claims.getSubject();
        String jti = claims.getId();
        if (!refreshTokenService.verifyRefreshToken(username, jti)) {
            throw new AppException("Refresh token has been revoked", HttpStatus.UNAUTHORIZED);
        }
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails.getUsername(),
                null,
                userDetails.getAuthorities()
        );

        String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        Claims newRefreshClaims = jwtTokenProvider.getClaims(newRefreshToken);
        refreshTokenService.saveRefreshToken(username, newRefreshClaims.getId());
        return new TokenResponse(
                newAccessToken,
                newRefreshToken,
                "Bearer",
                jwtTokenProvider.getAccessExpirationInSeconds(),
                jwtTokenProvider.getRefreshExpirationInSeconds());

    }
}
