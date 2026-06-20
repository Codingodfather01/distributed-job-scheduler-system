/*package com.CodinGodfather.Scheduler.Repository;

import com.CodinGodfather.Scheduler.Entity.WorkerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkerRepository extends JpaRepository<WorkerEntity, String> {

    List<WorkerEntity> findByStatusAndLastHeartbeatBefore(
            String status,
            LocalDateTime time
    );
}*/