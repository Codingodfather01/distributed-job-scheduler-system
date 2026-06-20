package com.CodinGodfather.Scheduler.Kafka;

public enum JobEventType {
    JOB_CREATED,
    JOB_EXECUTED,
    JOB_FAILED,
    JOB_RETRY_SCHEDULED,
    JOB_SENT_TO_DLQ
}