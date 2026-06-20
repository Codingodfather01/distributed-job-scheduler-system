package com.CodinGodfather.Scheduler.Repository;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface JobExecutionRepository extends JpaRepository<JobExecutionEntity,Long>
{
    List<JobExecutionEntity> findByJobId(Long jobId);
    List<JobExecutionEntity>
    findByStatusAndNextRetryTimeBefore(
            String status,
            LocalDateTime now
    );
    List<JobExecutionEntity> findByStatus(String status);
    List<JobExecutionEntity>findByStatusAndLeaseExpiryBefore(String status,LocalDateTime now);
}
