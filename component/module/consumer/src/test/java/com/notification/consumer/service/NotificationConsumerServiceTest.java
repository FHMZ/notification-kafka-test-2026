package com.notification.consumer.service;

import com.notification.consumer.model.NotificationMessage;
import com.notification.consumer.service.impl.NotificationConsumerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class NotificationConsumerServiceTest {

    private NotificationConsumerService service;

    @BeforeEach
    void setup() {
        service = new NotificationConsumerServiceImpl();
    }

    @Test
    void process_shouldNotThrow_whenMessageIsValid() {
        var msg = new NotificationMessage(
                "id-1",
                "EMAIL",
                "test@mail.com",
                "Hello",
                "Body",
                Instant.now()
        );

        assertDoesNotThrow(() -> service.process(msg));
    }

    @Test
    void process_shouldThrow_whenSubjectContainsFail() {
        var msg = new NotificationMessage(
                "id-2",
                "EMAIL",
                "test@mail.com",
                "please FAIL now",
                "Body",
                Instant.now()
        );

        assertThrows(IllegalStateException.class, () -> service.process(msg));
    }

}
