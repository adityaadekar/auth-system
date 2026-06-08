package com.example.authz;

import java.time.Instant;

public record RevokedToken(
        String sessionId,
        String jwtId,
        Instant revokedAt
) {
}
