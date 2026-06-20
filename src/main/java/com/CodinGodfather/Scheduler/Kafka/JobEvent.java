package com.CodinGodfather.Scheduler.Kafka;

import java.time.LocalDateTime;

public class JobEvent {

    private JobEventType eventType;
    private Long jobId;
    private Long executionId;
    private String jobName;
    private String status;
    private String workerId;
    private LocalDateTime eventTime;

    public JobEvent() {
    }

    public JobEvent(JobEventType eventType, Long jobId, Long executionId,
                    String jobName, String status, String workerId,
                    LocalDateTime eventTime) {
        this.eventType = eventType;
        this.jobId = jobId;
        this.executionId = executionId;
        this.jobName = jobName;
        this.status = status;
        this.workerId = workerId;
        this.eventTime = eventTime;
    }

    public JobEventType getEventType() {
        return eventType;
    }

    public void setEventType(JobEventType eventType) {
        this.eventType = eventType;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }
}