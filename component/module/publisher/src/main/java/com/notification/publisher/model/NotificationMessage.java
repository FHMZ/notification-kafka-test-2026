package com.notification.publisher.model;

import java.time.Instant;

public record NotificationMessage(
        String id,
        String type,  // "EMAIL"
        String to,
        String subject,
        String body,
        Instant createdAt
) {
}
