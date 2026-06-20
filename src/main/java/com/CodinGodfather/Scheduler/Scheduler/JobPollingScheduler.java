package com.CodinGodfather.Scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import com.CodinGodfather.Scheduler.Repository.JobRepository;
import com.CodinGodfather.Scheduler.Service.JobExecutionService;
import com.CodinGodfather.Scheduler.Service.RedisLockService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class JobPollingScheduler {

    private static final Logger log =
            LoggerFactory.getLogger(JobPollingScheduler.class);
    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final JobExecutionService jobExecutionService;
    private final RedisLockService redisLockService;

    public JobPollingScheduler(JobRepository jobRepository,
                               JobExecutionRepository jobExecutionRepository,
                               JobExecutionService jobExecutionService,
                               RedisLockService redisLockService) {
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobExecutionService = jobExecutionService;
        this.redisLockService = redisLockService;
    }

    @Scheduled(fixedRate = 5000)
    public void pollDueJobs() {

        boolean lockAcquired = redisLockService.acquireLock(
                "JOB_POLLING_LOCK",
                "worker-1",
                10
        );

        if (!lockAcquired) {
            log.info("Job polling skipped | reason=lock_owned_by_another_instance");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        List<JobEntity> dueJobs =
                jobRepository.findByEnabledTrueAndNextRunTimeBefore(now);

        for (JobEntity job : dueJobs) {
            int claimed = jobRepository.claimJob(
                    job.getJobId(),
                    "worker-1",
                    LocalDateTime.now().plusSeconds(30),
                    LocalDateTime.now()
            );

            if (claimed == 0) {
                continue;
            }

            JobExecutionEntity execution = new JobExecutionEntity();

            execution.setJobId(job.getJobId());
            execution.setStatus("PENDING");
            execution.setAttemptNumber(1);
            execution.setCreatedAt(now);
            execution.setUpdatedAt(now);

            execution = jobExecutionRepository.save(execution);

            execution.setOriginalExecutionId(execution.getExecutionId());
            execution.setUpdatedAt(LocalDateTime.now());

            execution = jobExecutionRepository.save(execution);

            jobExecutionService.executeAutomaticAttempt(job, execution);

            job.setNextRunTime(LocalDateTime.now().plusMinutes(1));

            // release claim after scheduling
            job.setLockedBy(null);
            job.setLockedUntil(null);

            job.setUpdatedAt(LocalDateTime.now());

            jobRepository.save(job);
        }
    }
}