package com.notification.consumer.dlq.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.notification.consumer.dlq.DeadLetterQueueDlq;
import com.notification.consumer.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
public class KafkaDeadLetterQueueDlq implements DeadLetterQueueDlq {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.topic.email-dlq}")
    private String dlqTopic;

    public KafkaDeadLetterQueueDlq(
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public record DlqEnvelope(
            String id,
            Instant failedAt,
            String reason,
            NotificationMessage message
    ) {
    }

    @Override
    public void send(NotificationMessage message, String reason) {
        final DlqEnvelope envelope = new DlqEnvelope(
                UUID.randomUUID().toString(),
                Instant.now(),
                reason,
                message
        );

        final String key = (message != null && message.id() != null) ? message.id() : envelope.id();

        try {
            final String json = objectMapper.writeValueAsString(envelope);
            kafkaTemplate.send(dlqTopic, key, json);

            // log claro de falha permanente / roteamento para DLQ
            log.error("Sent message to DLQ. topic={} key={} reason={}", dlqTopic, key, reason);

        } catch (JsonProcessingException e) {
            // não pode quebrar o consumer por falha ao serializar DLQ
            log.error("Failed to serialize DLQ envelope. topic={} reason={} error={}",
                    dlqTopic, reason, e.getMessage());
        }
    }
}
