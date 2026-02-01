package com.notification.auth.dto;

import java.time.Instant;
import java.util.Map;

public record ErrorDTO(
        int status,
        String error,
        String message,
        Map<String, String> fields
) {
    public ErrorDTO(int status, String error, String message) {
        this(status, error, message, null);
    }
}
