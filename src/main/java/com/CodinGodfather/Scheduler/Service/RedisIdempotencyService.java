package com.CodinGodfather.Scheduler.Service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisIdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean markExecutionIfNotProcessed(Long executionId) {
        String key = "execution:processed:" + executionId;

        Boolean saved = redisTemplate.opsForValue().setIfAbsent(
                key,
                "PROCESSED",
                Duration.ofHours(24)
        );

        return Boolean.TRUE.equals(saved);
    }
}