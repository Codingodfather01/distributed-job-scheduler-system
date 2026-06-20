package com.CodinGodfather.Scheduler.Service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RedisLockService {

    private final StringRedisTemplate redisTemplate;

    public RedisLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean acquireLock(String lockName, String owner, int lockSeconds) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                lockName,
                owner,
                Duration.ofSeconds(lockSeconds)
        );

        return Boolean.TRUE.equals(acquired);
    }
}