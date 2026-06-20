package com.CodinGodfather.Scheduler.Kafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class JobEventProducer {
    private static final Logger log =
            LoggerFactory.getLogger(JobEventProducer.class);

    private static final String TOPIC = "job-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    public JobEventProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(JobEvent event) {

        String message =
                event.getEventType()
                        + " | JobId=" + event.getJobId()
                        + " | ExecutionId=" + event.getExecutionId()
                        + " | JobName=" + event.getJobName()
                        + " | Status=" + event.getStatus()
                        + " | WorkerId=" + event.getWorkerId()
                        + " | EventTime=" + event.getEventTime();

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.getJobId()),
                message
        );
        log.info("Kafka event published");
    }
}