package com.notification.consumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.notification.consumer.dlq.DeadLetterQueueDlq;
import com.notification.consumer.model.NotificationMessage;
import com.notification.consumer.retry.RetryExecutor;
import com.notification.consumer.retry.RetryPolicy;
import com.notification.consumer.service.NotificationConsumerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaNotificationConsumerListener {

    private final NotificationConsumerService processorService;
    private final RetryExecutor retryExecutor;
    private final DeadLetterQueueDlq deadLetterQueueDlq;
    private final RetryPolicy retryPolicy;
    private final ObjectMapper objectMapper;

    public KafkaNotificationConsumerListener(
            NotificationConsumerService notificationConsumerService,
            RetryExecutor retryExecutor,
            DeadLetterQueueDlq deadLetterQueueDlq,
            RetryPolicy retryPolicy
    ) {
        this.processorService = notificationConsumerService;
        this.retryExecutor = retryExecutor;
        this.deadLetterQueueDlq = deadLetterQueueDlq;
        this.retryPolicy = retryPolicy;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @KafkaListener(
            topics = "${app.kafka.topic.email}",
            groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(String payload) {
        final NotificationMessage message;

        try {
            message = objectMapper.readValue(payload, NotificationMessage.class);
            log.info("Received message: id={} type={} to={}", safeId(message), message.type(), message.to());
        } catch (Exception ex) {
            final String reason = "Invalid JSON payload: " + ex.getMessage();
            log.error(reason);

            deadLetterQueueDlq.send(
                    new NotificationMessage(null, "UNKNOWN", null, null, payload, null),
                    reason
            );
            return;
        }

        try {
            retryExecutor.execute(
                    "process-notification id=" + safeId(message),
                    retryPolicy,
                    () -> processorService.process(message)
            );

            log.info("Successfully processed message: id={}", safeId(message));

        } catch (Exception ex) {
            final String reason = "Permanent failure after " + retryPolicy.maxAttempts()
                    + " attempts: " + (ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());

            log.error(reason);

            deadLetterQueueDlq.send(message, reason);
        }
    }

    private String safeId(NotificationMessage message) {
        return (message == null || message.id() == null) ? "null" : message.id();
    }
}
