package com.example.authz;

import java.time.Instant;
import java.util.Set;

public record AuthenticatedPrincipal(
        String subject,
        String sessionId,
        String applicationId,
        String jwtId,
        StoreContext store,
        SalesmanContext salesman,
        ActorType actorType,
        Set<String> actorGroups,
        Instant expiresAt
) {
    public boolean hasActorType(ActorType type) {
        return actorType == type;
    }

    public boolean hasActorGroup(String group) {
        return actorGroups.contains(group);
    }
}
