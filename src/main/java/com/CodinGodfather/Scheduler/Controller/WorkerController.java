package com.CodinGodfather.Scheduler.Controller;

import com.CodinGodfather.Scheduler.Service.RedisWorkerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class WorkerController {

    private final RedisWorkerService redisWorkerService;

    public WorkerController(RedisWorkerService redisWorkerService) {
        this.redisWorkerService = redisWorkerService;
    }

    @GetMapping("/api/workers/alive")
    public Set<String> getAliveWorkers() {
        return redisWorkerService.getAliveWorkers();
    }
}