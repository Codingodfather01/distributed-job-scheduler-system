package com.CodinGodfather.Scheduler.Scheduler;

import com.CodinGodfather.Scheduler.Service.WorkerService;
import jakarta.annotation.PostConstruct;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WorkerHeartbeatScheduler {

    private final WorkerService workerService;

    public WorkerHeartbeatScheduler(WorkerService workerService) {
        this.workerService = workerService;
    }

    @PostConstruct
    public void registerWorkerOnStartup() {
        workerService.registerWorker();
    }

    @Scheduled(fixedRate = 5000)
    public void sendHeartbeat() {
        workerService.heartbeat();
    }

    @Scheduled(fixedRate = 10000)
    public void detectDeadWorkers() {
        workerService.markDeadWorkers();
    }
}