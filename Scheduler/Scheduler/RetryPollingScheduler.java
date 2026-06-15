package com.CodinGodfather.Scheduler.Scheduler;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import com.CodinGodfather.Scheduler.Repository.JobRepository;
import com.CodinGodfather.Scheduler.Service.JobExecutionService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class RetryPollingScheduler {

    private final JobExecutionRepository jobExecutionRepository;
    private final JobRepository jobRepository;
    private final JobExecutionService jobExecutionService;

    public RetryPollingScheduler(JobExecutionRepository jobExecutionRepository,
                                 JobRepository jobRepository,
                                 JobExecutionService jobExecutionService) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.jobRepository = jobRepository;
        this.jobExecutionService = jobExecutionService;
    }

    @Scheduled(fixedRate = 5000)
    public void pollRetryJobs() {

        LocalDateTime now = LocalDateTime.now();

        List<JobExecutionEntity> retryExecutions =
                jobExecutionRepository.findByStatusAndNextRetryTimeBefore(
                        "RETRY_PENDING",
                        now
                );

        for (JobExecutionEntity oldExecution : retryExecutions) {

            oldExecution.setLeaseExpiry(null);
            oldExecution.setStatus("RETRYING");
            oldExecution.setUpdatedAt(LocalDateTime.now());
            jobExecutionRepository.save(oldExecution);

            JobEntity job = jobRepository.findById(oldExecution.getJobId())
                    .orElseThrow(() -> new RuntimeException(
                            "Job not found: " + oldExecution.getJobId()
                    ));

            JobExecutionEntity retryExecution = new JobExecutionEntity();

            retryExecution.setJobId(oldExecution.getJobId());

            retryExecution.setOriginalExecutionId(
                    oldExecution.getOriginalExecutionId() == null
                            ? oldExecution.getExecutionId()
                            : oldExecution.getOriginalExecutionId()
            );

            retryExecution.setAttemptNumber(oldExecution.getAttemptNumber() + 1);
            retryExecution.setStatus("RETRY_PENDING");
            retryExecution.setCreatedAt(LocalDateTime.now());
            retryExecution.setUpdatedAt(LocalDateTime.now());

            retryExecution = jobExecutionRepository.save(retryExecution);

            jobExecutionService.executeAutomaticAttempt(job, retryExecution);
        }
    }
}