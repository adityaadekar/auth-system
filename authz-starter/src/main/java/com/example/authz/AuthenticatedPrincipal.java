package com.example.authz;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record AuthenticatedPrincipal(
        String subject,
        String sessionId,
        String applicationId,
        String jwtId,
        StoreContext store,
        SalesmanContext salesman,
        String actorType,
        Set<String> actorGroups,
        Instant expiresAt
) {
    public boolean hasActorType(String type) {
        return Objects.equals(actorType, type);
    }

    public boolean hasActorGroup(String group) {
        return actorGroups.contains(group);
    }
}
