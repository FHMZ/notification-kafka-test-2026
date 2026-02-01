package com.notification.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenResponseDTO(
        @NotBlank String accessToken,
        @NotBlank String tokenType
) {
}
