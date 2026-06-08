package com.example.authservice.auth;

import jakarta.validation.constraints.NotBlank;

public record ExchangeSessionRequest(@NotBlank String sessionToken) {
}
