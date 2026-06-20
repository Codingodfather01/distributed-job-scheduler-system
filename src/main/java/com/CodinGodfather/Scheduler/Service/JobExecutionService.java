package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JobExecutionService {

    private static final Logger log =
            LoggerFactory.getLogger(JobExecutionService.class);

    private final JobExecutionRepository jobExecutionRepository;
    private final HttpCallbackService httpCallbackService;
    private final RedisIdempotencyService redisIdempotencyService;

    @Value("${WORKER_ID:worker-1}")
    private String workerId;

    public JobExecutionService(JobExecutionRepository jobExecutionRepository,
                               HttpCallbackService httpCallbackService,
                               RedisIdempotencyService redisIdempotencyService) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.httpCallbackService = httpCallbackService;
        this.redisIdempotencyService = redisIdempotencyService;
    }

    public JobExecutionEntity executeAutomaticAttempt(JobEntity job, JobExecutionEntity execution) {

        boolean firstTime = redisIdempotencyService.markExecutionIfNotProcessed(
                execution.getExecutionId()
        );

        if (!firstTime) {
            log.warn(
                    "Duplicate execution skipped | jobId={} | executionId={} | workerId={}",
                    job.getJobId(),
                    execution.getExecutionId(),
                    workerId
            );

            execution.setStatus("DUPLICATE_SKIPPED");
            execution.setUpdatedAt(LocalDateTime.now());

            return jobExecutionRepository.save(execution);
        }

        LocalDateTime now = LocalDateTime.now();

        execution.setStatus("RUNNING");
        execution.setWorkerId(workerId);
        execution.setLeaseExpiry(now.plusSeconds(30));
        execution.setStartTime(now);
        execution.setUpdatedAt(now);

        jobExecutionRepository.save(execution);

        try {
            log.info(
                    "Job execution started | jobId={} | executionId={} | attempt={} | maxTry={} | workerId={}",
                    job.getJobId(),
                    execution.getExecutionId(),
                    execution.getAttemptNumber(),
                    job.getMaxTry(),
                    workerId
            );

            boolean callbackSuccess = httpCallbackService.executeCallback(
                    job.getCallbackUrl(),
                    job.getJobId(),
                    execution.getExecutionId()
            );

            if (!callbackSuccess) {
                throw new RuntimeException("Callback failed");
            }

            execution.setStatus("SUCCESS");
            execution.setLeaseExpiry(null);
            execution.setEndTime(LocalDateTime.now());
            execution.setUpdatedAt(LocalDateTime.now());

            log.info(
                    "Job execution completed | jobId={} | executionId={} | status={} | workerId={}",
                    job.getJobId(),
                    execution.getExecutionId(),
                    execution.getStatus(),
                    workerId
            );

        } catch (Exception e) {
            execution.setErrorMessage(e.getMessage());
            execution.setLeaseExpiry(null);
            execution.setEndTime(LocalDateTime.now());
            execution.setUpdatedAt(LocalDateTime.now());

            if (execution.getAttemptNumber() < job.getMaxTry()) {
                execution.setStatus("RETRY_PENDING");
                execution.setNextRetryTime(LocalDateTime.now().plusSeconds(15));

                log.warn(
                        "Job execution failed retry scheduled | jobId={} | executionId={} | attempt={} | maxTry={} | status={} | nextRetryTime={} | workerId={}",
                        job.getJobId(),
                        execution.getExecutionId(),
                        execution.getAttemptNumber(),
                        job.getMaxTry(),
                        execution.getStatus(),
                        execution.getNextRetryTime(),
                        workerId
                );
            } else {
                execution.setStatus("DLQ");

                log.error(
                        "Job execution failed moved to DLQ | jobId={} | executionId={} | attempt={} | maxTry={} | status={} | workerId={}",
                        job.getJobId(),
                        execution.getExecutionId(),
                        execution.getAttemptNumber(),
                        job.getMaxTry(),
                        execution.getStatus(),
                        workerId
                );
            }
        }

        return jobExecutionRepository.save(execution);
    }
}