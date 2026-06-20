package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class JobExecutionService {

    private final JobExecutionRepository jobExecutionRepository;
    private final HttpCallbackService httpCallbackService;

    @Value("${WORKER_ID:worker-1}")
    private String workerId;

    public JobExecutionService(JobExecutionRepository jobExecutionRepository,
                               HttpCallbackService httpCallbackService) {
        this.jobExecutionRepository = jobExecutionRepository;
        this.httpCallbackService = httpCallbackService;
    }

    public JobExecutionEntity executeAutomaticAttempt(JobEntity job, JobExecutionEntity execution) {

        LocalDateTime now = LocalDateTime.now();

        execution.setStatus("RUNNING");
        execution.setWorkerId(workerId);
        execution.setLeaseExpiry(now.plusSeconds(30));
        execution.setStartTime(now);
        execution.setUpdatedAt(now);

        jobExecutionRepository.save(execution);

        try {
            System.out.println("Executing job " + job.getName()
                    + " attempt " + execution.getAttemptNumber());
            System.out.println("=================================");
            System.out.println("Executing job: " + job.getName());
            System.out.println("Callback URL: " + job.getCallbackUrl());
            System.out.println("Execution ID: " + execution.getExecutionId());
            System.out.println("Worker ID: " + workerId);
            System.out.println("=================================");

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

        } catch (Exception e) {
            execution.setErrorMessage(e.getMessage());
            execution.setLeaseExpiry(null);
            execution.setEndTime(LocalDateTime.now());
            execution.setUpdatedAt(LocalDateTime.now());

            if (execution.getAttemptNumber() < job.getMaxTry()) {
                execution.setStatus("RETRY_PENDING");
                execution.setNextRetryTime(LocalDateTime.now().plusSeconds(15));
            } else {
                execution.setStatus("DLQ");
            }
        }

        return jobExecutionRepository.save(execution);
    }
}