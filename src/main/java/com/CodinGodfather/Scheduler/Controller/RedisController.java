package com.CodinGodfather.Scheduler.Controller;

import com.CodinGodfather.Scheduler.Service.RedisLockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisController {

    private final RedisLockService redisLockService;

    public RedisController(RedisLockService redisLockService) {
        this.redisLockService = redisLockService;
    }

    @GetMapping("/redis-test")
    public String testRedis() {

        boolean acquired =
                redisLockService.acquireLock(
                        "TEST_LOCK",
                        "worker-1",
                        30
                );

        return "Lock Acquired = " + acquired;
    }
}