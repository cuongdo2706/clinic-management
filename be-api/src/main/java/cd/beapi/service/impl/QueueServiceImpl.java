package cd.beapi.service.impl;

import cd.beapi.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private static final String QUEUE_KEY_PREFIX = "queue:";

    /**
     * Lua script: INCR + EXPIREAT chạy atomic trong Redis.
     * ARGV[1] = unix timestamp lúc hết hạn (00:00 ngày hôm sau)
     * Trả về số thứ tự mới sau khi tăng.
     */
    private static final DefaultRedisScript<Long> INCR_EXPIRE_AT_SCRIPT = new DefaultRedisScript<>(
            """
            local num = redis.call('INCR', KEYS[1])
            if num == 1 then
                redis.call('EXPIREAT', KEYS[1], ARGV[1])
            end
            return num
            """,
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    @Override
    public int nextQueueNumberForDate(LocalDate date) {
        String key = QUEUE_KEY_PREFIX + date; // vd: "queue:2026-04-15"

        // Unix timestamp lúc 00:00 ngày kế tiếp → Redis dùng EXPIREAT
        long expireAtUnix = date.plusDays(1)
                .atStartOfDay()
                .toEpochSecond(ZoneOffset.of("+07:00")); // đổi theo timezone thực tế

        Long number = redisTemplate.execute(INCR_EXPIRE_AT_SCRIPT, List.of(key), String.valueOf(expireAtUnix));
        return number.intValue();
    }
}

