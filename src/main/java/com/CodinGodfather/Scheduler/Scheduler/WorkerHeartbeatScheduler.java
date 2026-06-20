package com.CodinGodfather.Scheduler.Scheduler;

import com.CodinGodfather.Scheduler.Service.RedisWorkerService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkerHeartbeatScheduler {

    private final RedisWorkerService redisWorkerService;

    public WorkerHeartbeatScheduler(RedisWorkerService redisWorkerService) {
        this.redisWorkerService = redisWorkerService;
    }

    @PostConstruct
    public void registerWorkerOnStartup() {
        redisWorkerService.sendHeartbeat();
    }

    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        redisWorkerService.sendHeartbeat();
    }
}