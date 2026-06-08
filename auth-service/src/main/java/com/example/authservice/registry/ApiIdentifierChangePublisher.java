package com.example.authservice.registry;

public interface ApiIdentifierChangePublisher {
    void publishPolicyChanged(String apiIdentifier);
}
