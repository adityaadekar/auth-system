package com.example.authz;

import java.util.Map;

public record SalesmanContext(
        String salesmanId,
        String displayName,
        String actorType,
        Map<String, Object> attributes
) {
}
