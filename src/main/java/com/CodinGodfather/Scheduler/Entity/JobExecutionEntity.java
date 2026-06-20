package com.CodinGodfather.Scheduler.Entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="job_execution")
public class JobExecutionEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long executionId;

    private Long jobId;

    private String status;
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private LocalDateTime leaseExpiry;

    private String workerId;
    @Lob
    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getOriginalExecutionId() {
        return originalExecutionId;
    }

    public void setOriginalExecutionId(Long originalExecutionId) {
        this.originalExecutionId = originalExecutionId;
    }

    public LocalDateTime getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(LocalDateTime nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    private Long originalExecutionId;
    private LocalDateTime nextRetryTime;

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    private int attemptNumber;

    public Long getExecutionId() {
        return executionId;
    }

    public void setExecutionId(Long executionId) {
        this.executionId = executionId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getLeaseExpiry() {
        return leaseExpiry;
    }

    public void setLeaseExpiry(LocalDateTime leaseExpiry) {
        this.leaseExpiry = leaseExpiry;
    }

    public String getWorkerId() {
        return workerId;
    }

    public void setWorkerId(String workerId) {
        this.workerId = workerId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
