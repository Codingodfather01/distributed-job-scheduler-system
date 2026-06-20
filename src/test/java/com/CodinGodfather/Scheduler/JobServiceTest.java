package com.CodinGodfather.Scheduler;

import com.CodinGodfather.Scheduler.Entity.JobEntity;
import com.CodinGodfather.Scheduler.Entity.JobExecutionEntity;
import com.CodinGodfather.Scheduler.Kafka.JobEventProducer;
import com.CodinGodfather.Scheduler.Repository.JobExecutionRepository;
import com.CodinGodfather.Scheduler.Repository.JobRepository;
import com.CodinGodfather.Scheduler.Service.HttpCallbackService;
import com.CodinGodfather.Scheduler.Service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobExecutionRepository jobExecutionRepository;

    @Mock
    private HttpCallbackService httpCallbackService;

    @Mock
    private JobEventProducer jobEventProducer;

    @InjectMocks
    private JobService jobService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testManualRetryFromDLQSuccess() {
        JobExecutionEntity oldExecution = new JobExecutionEntity();
        oldExecution.setExecutionId(10L);
        oldExecution.setJobId(1L);
        oldExecution.setStatus("DLQ");
        oldExecution.setAttemptNumber(3);

        JobEntity job = new JobEntity();
        job.setJobId(1L);
        job.setName("Test Job");
        job.setCallbackUrl("http://test.com");

        when(jobExecutionRepository.findById(10L))
                .thenReturn(Optional.of(oldExecution));

        when(jobRepository.findById(1L))
                .thenReturn(Optional.of(job));

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> {
                    JobExecutionEntity execution = invocation.getArgument(0);

                    if (execution.getExecutionId() == null) {
                        execution.setExecutionId(20L);
                    }

                    return execution;
                });

        when(httpCallbackService.executeCallback(
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(true);

        JobExecutionEntity result = jobService.manualRetryFromDLQ(10L);

        assertEquals("SUCCESS", result.getStatus());
        assertEquals("manual-worker", result.getWorkerId());
        assertEquals(4, result.getAttemptNumber());

        verify(jobEventProducer, atLeastOnce()).publish(any());
    }

    @Test
    void testManualRetryFromDLQCallbackFailsMovesToDLQ() {
        JobExecutionEntity oldExecution = new JobExecutionEntity();
        oldExecution.setExecutionId(11L);
        oldExecution.setJobId(2L);
        oldExecution.setStatus("DLQ");
        oldExecution.setAttemptNumber(3);

        JobEntity job = new JobEntity();
        job.setJobId(2L);
        job.setName("Failed Retry Job");
        job.setCallbackUrl("http://test.com");

        when(jobExecutionRepository.findById(11L))
                .thenReturn(Optional.of(oldExecution));

        when(jobRepository.findById(2L))
                .thenReturn(Optional.of(job));

        when(jobExecutionRepository.save(any(JobExecutionEntity.class)))
                .thenAnswer(invocation -> {
                    JobExecutionEntity execution = invocation.getArgument(0);

                    if (execution.getExecutionId() == null) {
                        execution.setExecutionId(21L);
                    }

                    return execution;
                });

        when(httpCallbackService.executeCallback(
                anyString(),
                anyLong(),
                anyLong()))
                .thenReturn(false);

        JobExecutionEntity result = jobService.manualRetryFromDLQ(11L);

        assertEquals("DLQ", result.getStatus());
        assertEquals("manual-worker", result.getWorkerId());
        assertEquals(4, result.getAttemptNumber());

        verify(jobEventProducer, atLeastOnce()).publish(any());
    }

    @Test
    void testManualRetryFromDLQThrowsExceptionWhenExecutionIsNotDLQ() {
        JobExecutionEntity oldExecution = new JobExecutionEntity();
        oldExecution.setExecutionId(12L);
        oldExecution.setJobId(3L);
        oldExecution.setStatus("SUCCESS");
        oldExecution.setAttemptNumber(1);

        when(jobExecutionRepository.findById(12L))
                .thenReturn(Optional.of(oldExecution));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> jobService.manualRetryFromDLQ(12L)
        );

        assertEquals(
                "Only DLQ executions can be manually retried",
                exception.getMessage()
        );

        verify(httpCallbackService, never())
                .executeCallback(anyString(), anyLong(), anyLong());
    }
}