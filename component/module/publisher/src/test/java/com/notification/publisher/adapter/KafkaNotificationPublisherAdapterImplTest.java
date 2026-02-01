package com.notification.publisher.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.notification.publisher.adapter.impl.KafkaNotificationPublisherAdapterImpl;
import com.notification.publisher.model.NotificationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationPublisherAdapterImplTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void publish_shouldSendToKafkaWithSerializedJson() {
        String topic = "notifications.email";
        var adapter = new KafkaNotificationPublisherAdapterImpl(kafkaTemplate, objectMapper, topic);

        NotificationMessage msg = new NotificationMessage(
                UUID.randomUUID().toString(),
                "EMAIL",
                "user@example.com",
                "Hello",
                "Body",
                Instant.now()
        );

        // simula send async ok
        when(kafkaTemplate.send(anyString(), anyString(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        adapter.publish(msg);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);

        verify(kafkaTemplate).send(topicCaptor.capture(), keyCaptor.capture(), payloadCaptor.capture());

        assertEquals(topic, topicCaptor.getValue());
        assertEquals(msg.id(), keyCaptor.getValue());

        String payload = payloadCaptor.getValue();
        assertNotNull(payload);
        assertTrue(payload.contains(msg.id()));
        assertTrue(payload.contains("\"type\":\"EMAIL\""));
        assertTrue(payload.contains("user@example.com"));
    }

    @Test
    void publish_shouldThrowWhenSerializationFails() {
        String topic = "notifications.email";

        // ObjectMapper "quebrado" para forçar exceção
        ObjectMapper broken = mock(ObjectMapper.class);
        var adapter = new KafkaNotificationPublisherAdapterImpl(kafkaTemplate, broken, topic);

        NotificationMessage msg = new NotificationMessage(
                "1", "EMAIL", "user@example.com", "Hello", "Body", Instant.now()
        );

        try {
            when(broken.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("boom") {});
        } catch (Exception ignored) { }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> adapter.publish(msg));
        assertTrue(ex.getMessage().contains("Failed to serialize"));

        verifyNoInteractions(kafkaTemplate);
    }
}
