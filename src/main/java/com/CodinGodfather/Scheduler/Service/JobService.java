package com.CodinGodfather.Scheduler.Service;

import com.CodinGodfather.Scheduler.DTO.CreateJobRequest;
import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Kafka.JobEvent;
import com.CodinGodfather.Scheduler.Kafka.JobEventProducer;
import com.CodinGodfather.Scheduler.Kafka.JobEventType;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import com.CodinGodfather.Scheduler.Repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    private static final Logger log =
            LoggerFactory.getLogger(JobService.class);

    private final JobRepository jobRepository;
    private final JobExecutionRepository jobExecutionRepository;
    private final HttpCallbackService httpCallbackService;
    private final JobEventProducer jobEventProducer;

    public JobService(JobRepository jobRepository,
                      JobExecutionRepository jobExecutionRepository,
                      HttpCallbackService httpCallbackService,
                      JobEventProducer jobEventProducer) {
        this.jobRepository = jobRepository;
        this.jobExecutionRepository = jobExecutionRepository;
        this.httpCallbackService = httpCallbackService;
        this.jobEventProducer = jobEventProducer;
    }

    public JobEntity createJob(CreateJobRequest request) {
        LocalDateTime now = LocalDateTime.now();

        JobEntity job = new JobEntity();

        job.setName(request.getName());
        job.setDescription(request.getDescription());
        job.setCronExpression(request.getCronExpression());
        job.setEnabled(true);
        job.setCreatedAt(now);
        job.setUpdatedAt(now);
        job.setCallbackUrl(request.getCallbackUrl());
        job.setMaxTry(3);
        job.setNextRunTime(now.plusMinutes(1));

        JobEntity savedJob = jobRepository.save(job);

        jobEventProducer.publish(new JobEvent(
                JobEventType.JOB_CREATED,
                savedJob.getJobId(),
                null,
                savedJob.getName(),
                "CREATED",
                null,
                LocalDateTime.now()
        ));

        return savedJob;
    }

    public List<JobEntity> getAllJobs() {
        return jobRepository.findAll();
    }

    public JobEntity getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
    }

    public List<JobExecutionEntity> getExecutionsByJobId(Long jobId) {
        return jobExecutionRepository.findByJobId(jobId);
    }

    public List<JobExecutionEntity> getDLQStatus() {
        return jobExecutionRepository.findByStatus("DLQ");
    }

    public JobExecutionEntity manualRetryFromDLQ(Long executionId) {

        JobExecutionEntity oldExecution = jobExecutionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

        if (!"DLQ".equals(oldExecution.getStatus())) {
            throw new RuntimeException("Only DLQ executions can be manually retried");
        }

        JobEntity job = jobRepository.findById(oldExecution.getJobId())
                .orElseThrow(() -> new RuntimeException("Job not found: " + oldExecution.getJobId()));

        oldExecution.setStatus("MANUALLY_RETRIED");
        oldExecution.setUpdatedAt(LocalDateTime.now());
        jobExecutionRepository.save(oldExecution);

        LocalDateTime now = LocalDateTime.now();

        JobExecutionEntity newExecution = new JobExecutionEntity();

        newExecution.setJobId(oldExecution.getJobId());
        newExecution.setOriginalExecutionId(
                oldExecution.getOriginalExecutionId() == null
                        ? oldExecution.getExecutionId()
                        : oldExecution.getOriginalExecutionId()
        );
        newExecution.setAttemptNumber(oldExecution.getAttemptNumber() + 1);
        newExecution.setStatus("RUNNING");
        newExecution.setWorkerId("manual-worker");
        newExecution.setLeaseExpiry(now.plusSeconds(30));
        newExecution.setStartTime(now);
        newExecution.setCreatedAt(now);
        newExecution.setUpdatedAt(now);

        newExecution = jobExecutionRepository.save(newExecution);

        try {
            log.info("Manual retry requested | jobId={}", job.getJobId()
            );

            boolean callbackSuccess =
                    httpCallbackService.executeCallback(
                            job.getCallbackUrl(),
                            job.getJobId(),
                            newExecution.getExecutionId()
                    );

            if (!callbackSuccess) {
                throw new RuntimeException("Manual Retry Callback Failed");
            }

            newExecution.setStatus("SUCCESS");
            newExecution.setLeaseExpiry(null);
            newExecution.setEndTime(LocalDateTime.now());
            newExecution.setUpdatedAt(LocalDateTime.now());

            JobExecutionEntity savedExecution = jobExecutionRepository.save(newExecution);

            jobEventProducer.publish(new JobEvent(
                    JobEventType.JOB_EXECUTED,
                    job.getJobId(),
                    savedExecution.getExecutionId(),
                    job.getName(),
                    savedExecution.getStatus(),
                    savedExecution.getWorkerId(),
                    LocalDateTime.now()
            ));

            return savedExecution;

        } catch (Exception e) {
            newExecution.setStatus("DLQ");
            newExecution.setLeaseExpiry(null);
            newExecution.setErrorMessage(e.getMessage());
            newExecution.setEndTime(LocalDateTime.now());
            newExecution.setUpdatedAt(LocalDateTime.now());

            JobExecutionEntity savedExecution = jobExecutionRepository.save(newExecution);

            jobEventProducer.publish(new JobEvent(
                    JobEventType.JOB_SENT_TO_DLQ,
                    job.getJobId(),
                    savedExecution.getExecutionId(),
                    job.getName(),
                    savedExecution.getStatus(),
                    savedExecution.getWorkerId(),
                    LocalDateTime.now()
            ));

            return savedExecution;
        }
    }

    public void recoverExpiredLeases() {
        LocalDateTime now = LocalDateTime.now();

        List<JobExecutionEntity> expiredExecutions =
                jobExecutionRepository.findByStatusAndLeaseExpiryBefore("RUNNING", now);

        for (JobExecutionEntity execution : expiredExecutions) {

            JobEntity job = jobRepository.findById(execution.getJobId())
                    .orElseThrow(() -> new RuntimeException("Job not found: " + execution.getJobId()));

            if (execution.getAttemptNumber() < job.getMaxTry()) {

                execution.setStatus("RETRY_ATTEMPT_CREATED");
                execution.setLeaseExpiry(null);
                execution.setErrorMessage("Worker lease expired. Retry attempt created.");
                execution.setEndTime(now);
                execution.setUpdatedAt(now);

                jobExecutionRepository.save(execution);

                JobExecutionEntity retryExecution = new JobExecutionEntity();

                retryExecution.setJobId(execution.getJobId());
                retryExecution.setOriginalExecutionId(
                        execution.getOriginalExecutionId() == null
                                ? execution.getExecutionId()
                                : execution.getOriginalExecutionId()
                );
                retryExecution.setAttemptNumber(execution.getAttemptNumber() + 1);
                retryExecution.setStatus("RETRY_PENDING");
                retryExecution.setCreatedAt(now);
                retryExecution.setUpdatedAt(now);

                JobExecutionEntity savedRetryExecution =
                        jobExecutionRepository.save(retryExecution);

                jobEventProducer.publish(new JobEvent(
                        JobEventType.JOB_RETRY_SCHEDULED,
                        job.getJobId(),
                        savedRetryExecution.getExecutionId(),
                        job.getName(),
                        savedRetryExecution.getStatus(),
                        savedRetryExecution.getWorkerId(),
                        LocalDateTime.now()
                ));

            } else {

                execution.setStatus("DLQ");
                execution.setLeaseExpiry(null);
                execution.setErrorMessage("Worker lease expired and max attempts reached");
                execution.setEndTime(now);
                execution.setUpdatedAt(now);

                JobExecutionEntity savedExecution =
                        jobExecutionRepository.save(execution);

                jobEventProducer.publish(new JobEvent(
                        JobEventType.JOB_SENT_TO_DLQ,
                        job.getJobId(),
                        savedExecution.getExecutionId(),
                        job.getName(),
                        savedExecution.getStatus(),
                        savedExecution.getWorkerId(),
                        LocalDateTime.now()
                ));
            }
        }
    }
}