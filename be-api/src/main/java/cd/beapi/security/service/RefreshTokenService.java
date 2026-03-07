package cd.beapi.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final StringRedisTemplate redisTemplate;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshExpiration;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    //Save refresh token
    public void saveRefreshToken(String username, String jti) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.opsForValue().set(
                key,
                jti,
                refreshExpiration,
                TimeUnit.MILLISECONDS
        );
    }

    //Get refresh token
    public String getRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        return redisTemplate.opsForValue().get(key);
    }

    //Delete refresh token
    public void deleteRefreshToken(String username) {
        String key = REFRESH_TOKEN_PREFIX + username;
        redisTemplate.delete(key);
    }

    //Verify refresh token
    public boolean verifyRefreshToken(String username, String jti) {
        String storedJti = getRefreshToken(username);
        if (storedJti == null) return false;
        return storedJti.equals(jti);
    }
}
