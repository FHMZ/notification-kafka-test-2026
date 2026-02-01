package com.notification.publisher.service;

import com.notification.publisher.adapter.NotificationPublisherAdapter;
import com.notification.publisher.dto.EmailNotificationRequestDTO;
import com.notification.publisher.model.NotificationMessage;
import com.notification.publisher.service.impl.NotificationPublisherServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class NotificationPublisherServiceTest {

    @Mock
    private NotificationPublisherAdapter publisherAdapter;

    @Test
    void publishEmail_shouldCreateMessageAndCallAdapter() {
        var service = new NotificationPublisherServiceImpl(publisherAdapter);

        EmailNotificationRequestDTO req = new EmailNotificationRequestDTO(
                "user@example.com",
                "Hello",
                "Body"
        );

        String id = service.publishEmail(req);

        assertNotNull(id);
        assertFalse(id.isBlank());

        ArgumentCaptor<NotificationMessage> captor = ArgumentCaptor.forClass(NotificationMessage.class);
        verify(publisherAdapter).publish(captor.capture());

        NotificationMessage msg = captor.getValue();
        assertEquals(id, msg.id());
        assertEquals("EMAIL", msg.type());
        assertEquals("user@example.com", msg.to());
        assertEquals("Hello", msg.subject());
        assertEquals("Body", msg.body());
        assertNotNull(msg.createdAt());
    }

}
