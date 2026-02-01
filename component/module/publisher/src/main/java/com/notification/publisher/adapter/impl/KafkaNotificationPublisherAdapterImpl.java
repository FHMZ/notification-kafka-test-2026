package com.notification.publisher.adapter.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.notification.publisher.adapter.NotificationPublisherAdapter;
import com.notification.publisher.model.NotificationMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class KafkaNotificationPublisherAdapterImpl implements NotificationPublisherAdapter {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaNotificationPublisherAdapterImpl(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic.email}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    @Override
    public void publish(NotificationMessage message) {
        final String payload;
        try {
            payload = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize message to JSON", e);
        }

        log.info("Publishing message to Kafka. topic={} id={} type={} to={}",
                topic, message.id(), message.type(), message.to());

        // send assíncrono — requisito do PDF é publicar, não esperar envio.
        kafkaTemplate.send(topic, message.id(), payload)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka publish failed. topic={} id={} error={}",
                                topic, message.id(), ex.getMessage());
                        return;
                    }
                    var meta = result.getRecordMetadata();
                    log.info("Kafka publish ok. topic={} id={} partition={} offset={}",
                            meta.topic(), message.id(), meta.partition(), meta.offset());
                });
    }
}