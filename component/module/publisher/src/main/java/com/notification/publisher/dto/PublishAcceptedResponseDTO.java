package com.notification.publisher.dto;

import java.time.Instant;

public record PublishAcceptedResponseDTO(
        String notificationId,
        String status,
        Instant timestamp
) {
    public static PublishAcceptedResponseDTO accepted(String id) {
        return new PublishAcceptedResponseDTO(id, "ACCEPTED", Instant.now());
    }
}
