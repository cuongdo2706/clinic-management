package cd.beapi.security.jwt;

import cd.beapi.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

@RequiredArgsConstructor
public class BlacklistTokenValidator implements OAuth2TokenValidator<Jwt> {
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public OAuth2TokenValidatorResult validate(Jwt token) {
        String jti = token.getId();
        if (jti != null && tokenBlacklistService.isBlacklisted(jti)){
            OAuth2Error error = new OAuth2Error(
                    "Token revoked",
                    "Token has been revoked",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
        String type = token.getClaimAsString("type");
        if (!"access".equals(type)){
            OAuth2Error error = new OAuth2Error(
                    "invalid_token_type",
                    "Token type must be 'access'",
                    null
            );
            return OAuth2TokenValidatorResult.failure(error);
        }
        return OAuth2TokenValidatorResult.success();
    }
}
