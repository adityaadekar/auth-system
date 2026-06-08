package com.example.authservice.auth;

import java.time.Instant;

public record JwtIssueResponse(
        String tokenType,
        String jwtToken,
        Instant expiresAt
) {
}
