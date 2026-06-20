package com.CodinGodfather.Scheduler.Kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class JobEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(JobEventConsumer.class);

    @KafkaListener(
            topics = "job-events",
            groupId = "scheduler-group"
    )
    public void consumeJobEvent(String message) {

        log.info("Kafka event consumed");
    }
}