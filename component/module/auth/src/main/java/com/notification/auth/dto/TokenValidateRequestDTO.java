package com.notification.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenValidateRequestDTO(
        @NotBlank String token
) {
}
