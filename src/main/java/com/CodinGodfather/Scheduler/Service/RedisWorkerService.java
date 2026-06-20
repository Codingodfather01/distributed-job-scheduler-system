package com.CodinGodfather.Scheduler.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class RedisWorkerService {

    private static final Logger log =
            LoggerFactory.getLogger(RedisWorkerService.class);

    private final StringRedisTemplate redisTemplate;

    @Value("${WORKER_ID:worker-1}")
    private String workerId;

    public RedisWorkerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void sendHeartbeat() {
        String key = "worker:" + workerId;

        redisTemplate.opsForValue().set(
                key,
                "ACTIVE",
                Duration.ofSeconds(30)
        );

        log.info("Redis worker heartbeat sent | workerId={}", workerId);
    }

    public Set<String> getAliveWorkers() {
        return redisTemplate.keys("worker:*");
    }
}