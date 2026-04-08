package cd.beapi.service.impl;

import cd.beapi.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private static final String QUEUE_KEY = "queue";

    private final StringRedisTemplate redisTemplate;

    @Override
    public int nextQueueNumber() {
        Long number = redisTemplate.opsForValue().increment(QUEUE_KEY);

        // Lần đầu trong ngày → set TTL hết hạn lúc 00:00 ngày mai → tự reset
        if (Long.valueOf(1L).equals(number)) {
            LocalDateTime midnight = LocalDate.now().plusDays(1).atTime(LocalTime.MIDNIGHT);
            Duration ttl = Duration.between(LocalDateTime.now(), midnight);
            redisTemplate.expire(QUEUE_KEY, ttl);
        }

        return number.intValue();
    }
}

