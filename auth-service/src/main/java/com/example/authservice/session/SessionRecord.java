package com.example.authservice.session;

import java.time.Instant;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

public record SessionRecord(
        String sessionId,
        String sessionToken,
        String applicationId,
        String deviceId,
        StoreContext store,
        SalesmanContext salesman,
        Instant expiresAt,
        Instant createdAt,
        boolean revoked
) {
    public boolean isExpired(Instant now) {
        return !expiresAt.isAfter(now);
    }

    public SessionRecord revoke() {
        return new SessionRecord(sessionId, sessionToken, applicationId, deviceId, store, salesman, expiresAt, createdAt, true);
    }
}
