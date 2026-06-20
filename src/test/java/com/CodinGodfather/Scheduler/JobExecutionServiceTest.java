package com.CodinGodfather.Scheduler;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import com.CodinGodfather.Scheduler.Service.HttpCallbackService;
import com.CodinGodfather.Scheduler.Service.JobExecutionService;
import com.CodinGodfather.Scheduler.Service.RedisIdempotencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobExecutionServiceTest {

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private HttpCallbackService httpCallbackService;

    @Mock
    private RedisIdempotencyService redisIdempotencyService;

    @InjectMocks
    private JobExecutionService jobExecutionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testExecuteAutomaticAttemptSuccess() {

        JobEntity job = new JobEntity();
        job.setJobId(1L);
        job.setName("Test Job");
        job.setMaxTry(3);
        job.setCallbackUrl("http://test.com");

        JobExecutionEntity execution = new JobExecutionEntity();
        execution.setExecutionId(100L);
        execution.setAttemptNumber(1);

        when(redisIdempotencyService.markExecutionIfNotProcessed(100L))
                .thenReturn(true);

        when(httpCallbackService.executeCallback(
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(true);

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobExecutionEntity result =
                jobExecutionService.executeAutomaticAttempt(job, execution);

        assertEquals("SUCCESS", result.getStatus());

        verify(jobExecutionRepository, atLeastOnce())
                .save(any(JobExecutionEntity.class));
    }

    @Test
    void testExecuteAutomaticAttemptRetryPending() {

        JobEntity job = new JobEntity();
        job.setJobId(2L);
        job.setName("Retry Job");
        job.setMaxTry(3);
        job.setCallbackUrl("http://test.com");

        JobExecutionEntity execution = new JobExecutionEntity();
        execution.setExecutionId(200L);
        execution.setAttemptNumber(1);

        when(redisIdempotencyService.markExecutionIfNotProcessed(200L))
                .thenReturn(true);

        when(httpCallbackService.executeCallback(
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(false);

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobExecutionEntity result =
                jobExecutionService.executeAutomaticAttempt(job, execution);

        assertEquals("RETRY_PENDING", result.getStatus());

        verify(jobExecutionRepository, atLeastOnce())
                .save(any(JobExecutionEntity.class));
    }

    @Test
    void testExecuteAutomaticAttemptMovesToDLQ() {

        JobEntity job = new JobEntity();
        job.setJobId(3L);
        job.setName("DLQ Job");
        job.setMaxTry(3);
        job.setCallbackUrl("http://test.com");

        JobExecutionEntity execution = new JobExecutionEntity();
        execution.setExecutionId(300L);
        execution.setAttemptNumber(3);

        when(redisIdempotencyService.markExecutionIfNotProcessed(300L))
                .thenReturn(true);

        when(httpCallbackService.executeCallback(
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(false);

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobExecutionEntity result =
                jobExecutionService.executeAutomaticAttempt(job, execution);

        assertEquals("DLQ", result.getStatus());

        verify(jobExecutionRepository, atLeastOnce())
                .save(any(JobExecutionEntity.class));
    }

    @Test
    void testExecuteAutomaticAttemptDuplicateSkipped() {

        JobEntity job = new JobEntity();
        job.setJobId(4L);
        job.setName("Duplicate Job");
        job.setMaxTry(3);
        job.setCallbackUrl("http://test.com");

        JobExecutionEntity execution = new JobExecutionEntity();
        execution.setExecutionId(400L);
        execution.setAttemptNumber(1);

        when(redisIdempotencyService.markExecutionIfNotProcessed(400L))
                .thenReturn(false);

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        JobExecutionEntity result =
                jobExecutionService.executeAutomaticAttempt(job, execution);

        assertEquals("DUPLICATE_SKIPPED", result.getStatus());

        verify(httpCallbackService, never())
                .executeCallback(anyString(), anyLong(), anyLong());

        verify(jobExecutionRepository, atLeastOnce())
                .save(any(JobExecutionEntity.class));
    }
}