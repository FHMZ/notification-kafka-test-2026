package com.notification.publisher.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailNotificationRequestDTO(
        @NotBlank @Email String to,
        @NotBlank String subject,
        @NotBlank String body
) {
}
