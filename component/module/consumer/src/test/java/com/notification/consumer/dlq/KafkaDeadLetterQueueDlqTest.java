package com.notification.consumer.dlq;

import com.notification.consumer.dlq.impl.KafkaDeadLetterQueueDlq;
import com.notification.consumer.model.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KafkaDeadLetterQueueDlqTest {

    private static final String DLQ_TOPIC = "notifications.email.dlq";

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;


    private KafkaDeadLetterQueueDlq adapter;

    @BeforeEach
    void setup() {
        adapter = new KafkaDeadLetterQueueDlq(kafkaTemplate);
        setDlqTopic(adapter, DLQ_TOPIC);
    }

    @Test
    void send_shouldPublishEnvelopeToDlqTopic() {
        NotificationMessage message = new NotificationMessage(
                UUID.randomUUID().toString(),
                "EMAIL",
                "user@example.com",
                "FAIL",
                "Body",
                Instant.now()
        );

        adapter.send(message, "Permanent failure after 3 attempts: simulated");

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), valueCaptor.capture());

        assertEquals(DLQ_TOPIC, topicCaptor.getValue());
        assertEquals(message.id(), keyCaptor.getValue());

        String json = valueCaptor.getValue();
        assertNotNull(json);
        assertFalse(json.isBlank());

        // valida estrutura mínima do envelope
        assertTrue(json.contains("\"reason\""));
        assertTrue(json.contains("\"failedAt\""));
        assertTrue(json.contains("\"message\""));

        // valida que a mensagem original foi embutida
        assertTrue(json.contains(message.id()));
        assertTrue(json.contains("user@example.com"));
    }

    @Test
    void send_shouldFallbackKey_whenMessageIdIsNull() {
        NotificationMessage message = new NotificationMessage(
                null,
                "EMAIL",
                "user@example.com",
                "FAIL",
                "Body",
                Instant.now()
        );

        adapter.send(message, "any reason");

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(kafkaTemplate).send(eq(DLQ_TOPIC), keyCaptor.capture(), anyString());

        assertNotNull(keyCaptor.getValue());
        assertFalse(keyCaptor.getValue().isBlank());
    }

    private static void setDlqTopic(KafkaDeadLetterQueueDlq adapter, String topic) {
        try {
            Field field = KafkaDeadLetterQueueDlq.class.getDeclaredField("dlqTopic");
            field.setAccessible(true);
            field.set(adapter, topic);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
