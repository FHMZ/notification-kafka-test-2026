package com.notification.publisher.service.impl;

import com.notification.publisher.adapter.NotificationPublisherAdapter;
import com.notification.publisher.dto.EmailNotificationRequestDTO;
import com.notification.publisher.model.NotificationMessage;
import com.notification.publisher.service.NotificationPublisherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
public class NotificationPublisherServiceImpl implements NotificationPublisherService {

    private final NotificationPublisherAdapter publisherAdapter;

    public NotificationPublisherServiceImpl(NotificationPublisherAdapter publisherAdapter) {
        this.publisherAdapter = publisherAdapter;
    }

    @Override
    public String publishEmail(EmailNotificationRequestDTO request) {
        final String id = UUID.randomUUID().toString();

        NotificationMessage message = new NotificationMessage(
                id,
                "EMAIL",
                request.to(),
                request.subject(),
                request.body(),
                Instant.now()
        );

        publisherAdapter.publish(message);

        log.info("Publish requested. id={} to={}", id, request.to());
        return id;
    }
}
