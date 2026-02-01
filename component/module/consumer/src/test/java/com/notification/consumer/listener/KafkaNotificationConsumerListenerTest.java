package com.notification.consumer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.notification.consumer.dlq.DeadLetterQueueDlq;
import com.notification.consumer.model.NotificationMessage;
import com.notification.consumer.retry.RetryExecutor;
import com.notification.consumer.retry.RetryPolicy;
import com.notification.consumer.service.NotificationConsumerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class KafkaNotificationConsumerListenerTest {

    @Mock
    private NotificationConsumerService notificationConsumerService;

    @Mock
    private RetryExecutor retryExecutor;

    @Mock
    private DeadLetterQueueDlq deadLetterQueueDlq;

    private KafkaNotificationConsumerListener listener;

    private ObjectMapper objectMapper;
    private RetryPolicy retryPolicy;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        retryPolicy = RetryPolicy.of(3, 0);

        listener = new KafkaNotificationConsumerListener(
                notificationConsumerService,
                retryExecutor,
                deadLetterQueueDlq,
                retryPolicy
        );
    }

    @Test
    void consume_shouldProcessMessage_whenJsonIsValid() throws Exception {
        NotificationMessage message = validMessage();
        String payload = objectMapper.writeValueAsString(message);

        // Simula o RetryExecutor chamando a action com sucesso
        doAnswer(invocation -> {
            RetryExecutor.ThrowingRunnable action = invocation.getArgument(2);
            action.run();
            return null;
        }).when(retryExecutor).execute(anyString(), eq(retryPolicy), any(RetryExecutor.ThrowingRunnable.class));

        listener.consume(payload);

        verify(retryExecutor).execute(contains("process-notification"), eq(retryPolicy), any(RetryExecutor.ThrowingRunnable.class));
        verify(notificationConsumerService).process(any(NotificationMessage.class));
        verifyNoInteractions(deadLetterQueueDlq);
    }

    @Test
    void consume_shouldSendToDlq_whenJsonIsInvalid() {
        String invalidJson = "{ invalid-json }";

        listener.consume(invalidJson);

        ArgumentCaptor<NotificationMessage> msgCaptor = ArgumentCaptor.forClass(NotificationMessage.class);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);

        verify(deadLetterQueueDlq).send(msgCaptor.capture(), reasonCaptor.capture());
        verifyNoInteractions(notificationConsumerService);
        verifyNoInteractions(retryExecutor);

        NotificationMessage dlqMsg = msgCaptor.getValue();
        String reason = reasonCaptor.getValue();

        assertEquals("UNKNOWN", dlqMsg.type());
        assertEquals(invalidJson, dlqMsg.body()); // payload original guardado no body
        assertTrue(reason.startsWith("Invalid JSON payload:"), "Reason should start with 'Invalid JSON payload:'");
    }

    @Test
    void consume_shouldSendToDlq_whenProcessingFailsAfterRetry() throws Exception {
        NotificationMessage message = validMessage();
        String payload = objectMapper.writeValueAsString(message);

        doThrow(new RuntimeException("processing failed"))
                .when(retryExecutor)
                .execute(anyString(), eq(retryPolicy), any(RetryExecutor.ThrowingRunnable.class));

        listener.consume(payload);

        ArgumentCaptor<NotificationMessage> msgCaptor = ArgumentCaptor.forClass(NotificationMessage.class);
        ArgumentCaptor<String> reasonCaptor = ArgumentCaptor.forClass(String.class);

        verify(deadLetterQueueDlq).send(msgCaptor.capture(), reasonCaptor.capture());

        // como o RetryExecutor foi mockado pra lançar direto, o service não roda
        verify(notificationConsumerService, never()).process(any());

        NotificationMessage dlqMsg = msgCaptor.getValue();
        String reason = reasonCaptor.getValue();

        assertEquals(message.id(), dlqMsg.id());
        assertTrue(reason.contains("Permanent failure after 3 attempts"), "Must include attempts count");
        assertTrue(reason.contains("processing failed"), "Must include original failure message");
    }

    private NotificationMessage validMessage() {
        return new NotificationMessage(
                UUID.randomUUID().toString(),
                "EMAIL",
                "test@mail.com",
                "Hello",
                "Body content",
                Instant.now()
        );
    }
}
