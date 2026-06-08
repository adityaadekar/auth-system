package com.example.authservice.registry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "auth.api-registry", name = "storage", havingValue = "memory")
public class NoOpApiIdentifierChangePublisher implements ApiIdentifierChangePublisher {
    @Override
    public void publishPolicyChanged(String apiIdentifier) {
    }
}
