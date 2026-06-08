package com.example.authz;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JwtRevocationCache {
    private final AuthzProperties properties;
    private final ApiIdentifierRegistryClient client;
    private final Clock clock;
    private final Set<String> revokedSessionIds = ConcurrentHashMap.newKeySet();
    private final Set<String> revokedJwtIds = ConcurrentHashMap.newKeySet();
    private volatile Instant lastRefresh = Instant.EPOCH;

    public JwtRevocationCache(AuthzProperties properties, ApiIdentifierRegistryClient client, Clock clock) {
        this.properties = properties;
        this.client = client;
        this.clock = clock;
        refresh();
    }

    public boolean isRevoked(AuthenticatedPrincipal principal) {
        refreshIfStale();
        return revokedSessionIds.contains(principal.sessionId()) || revokedJwtIds.contains(principal.jwtId());
    }

    public void refresh() {
        client.fetchRevocations().ifPresent(revocations -> {
            revokedSessionIds.clear();
            revokedJwtIds.clear();
            for (RevokedToken revocation : revocations) {
                if (revocation.sessionId() != null) {
                    revokedSessionIds.add(revocation.sessionId());
                }
                if (revocation.jwtId() != null) {
                    revokedJwtIds.add(revocation.jwtId());
                }
            }
        });
        lastRefresh = clock.instant();
    }

    private void refreshIfStale() {
        if (lastRefresh.plus(properties.getRegistryRefreshInterval()).isBefore(clock.instant())) {
            refresh();
        }
    }
}
