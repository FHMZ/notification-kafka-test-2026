package com.notification.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record UserMeDTO(
        @NotBlank String username
) {
}
