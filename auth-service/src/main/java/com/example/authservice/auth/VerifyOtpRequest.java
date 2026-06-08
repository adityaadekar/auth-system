package com.example.authservice.auth;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(
        @NotBlank String salesmanId,
        @NotBlank String otp,
        @NotBlank String applicationId,
        String deviceId
) {
}
