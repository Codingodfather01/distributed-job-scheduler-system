/*package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.Entity.WorkerEntity;
import com.CodinGodfather.Scheduler.Repository.WorkerRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkerService {

    private final WorkerRepository workerRepository;

    @Value("${WORKER_ID:worker-1}")
    private String workerId;

    public WorkerService(WorkerRepository workerRepository) {
        this.workerRepository = workerRepository;
    }

    public void registerWorker() {
        LocalDateTime now = LocalDateTime.now();

        WorkerEntity worker = workerRepository.findById(workerId)
                .orElse(new WorkerEntity());

        worker.setWorkerId(workerId);
        worker.setHostname(getHostname());
        worker.setStatus("ACTIVE");
        worker.setLastHeartbeat(now);

        if (worker.getCreatedAt() == null) {
            worker.setCreatedAt(now);
        }

        worker.setUpdatedAt(now);

        workerRepository.save(worker);
    }

    public void heartbeat() {
        WorkerEntity worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new RuntimeException("Worker not registered: " + workerId));

        LocalDateTime now = LocalDateTime.now();

        worker.setStatus("ACTIVE");
        worker.setLastHeartbeat(now);
        worker.setUpdatedAt(now);

        workerRepository.save(worker);
    }

    public void markDeadWorkers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(30);

        List<WorkerEntity> deadWorkers =
                workerRepository.findByStatusAndLastHeartbeatBefore("ACTIVE", cutoffTime);

        for (WorkerEntity worker : deadWorkers) {
            worker.setStatus("DEAD");
            worker.setUpdatedAt(LocalDateTime.now());
            workerRepository.save(worker);
        }
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "unknown-host";
        }
    }
}*/