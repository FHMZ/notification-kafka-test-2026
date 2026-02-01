package com.notification.auth.dto;

public record TokenValidateResponseDTO(
        boolean valid,
        String subject
) {
}
