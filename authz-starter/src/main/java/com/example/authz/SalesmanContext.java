package com.example.authz;

import java.util.Map;

public record SalesmanContext(
        String salesmanId,
        String displayName,
        ActorType actorType,
        Map<String, Object> attributes
) {
}
