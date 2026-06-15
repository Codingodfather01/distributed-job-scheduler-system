package com.CodinGodfather.Scheduler.Repository;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface JobRepository extends JpaRepository<JobEntity, Long> {

    List<JobEntity> findByEnabledTrueAndNextRunTimeBefore(LocalDateTime now);

    @Modifying
    @Transactional
    @Query("""
            UPDATE JobEntity j
            SET j.lockedBy = :workerId,
                j.lockedUntil = :lockUntil
            WHERE j.jobId = :jobId
              AND j.enabled = true
              AND j.nextRunTime <= :now
              AND (j.lockedUntil IS NULL OR j.lockedUntil < :now)
            """)
    int claimJob(Long jobId,
                 String workerId,
                 LocalDateTime lockUntil,
                 LocalDateTime now);
}