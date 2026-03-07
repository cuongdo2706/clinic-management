package cd.beapi.security.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String jti, Date exp) {
        long ttl = exp.getTime() - System.currentTimeMillis();
        if (ttl > 0){
            redisTemplate.opsForValue().set(
                    BLACKLIST_PREFIX+jti,
                    "blacklisted",
                    ttl,
                    TimeUnit.MILLISECONDS
            );
        }
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}
