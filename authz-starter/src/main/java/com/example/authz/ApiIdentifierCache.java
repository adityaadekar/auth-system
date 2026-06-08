package com.example.authz;

import java.time.Clock;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ApiIdentifierCache {
    private final AuthzProperties properties;
    private final ApiIdentifierRegistryClient client;
    private final Clock clock;
    private final Map<String, ApiAccessPolicy> policies = new ConcurrentHashMap<>();
    private volatile Instant lastRefresh = Instant.EPOCH;

    public ApiIdentifierCache(AuthzProperties properties, ApiIdentifierRegistryClient client, Clock clock) {
        this.properties = properties;
        this.client = client;
        this.clock = clock;
        loadLocalPolicies();
        refresh();
    }

    public Optional<ApiAccessPolicy> find(String apiIdentifier) {
        refreshIfStale();
        return Optional.ofNullable(policies.get(apiIdentifier));
    }

    public void refresh() {
        client.fetchPolicies().ifPresent(this::replacePolicies);
        lastRefresh = clock.instant();
    }

    private void refreshIfStale() {
        if (lastRefresh.plus(properties.getRegistryRefreshInterval()).isBefore(clock.instant())) {
            refresh();
        }
    }

    private void replacePolicies(Collection<ApiAccessPolicy> remotePolicies) {
        policies.clear();
        loadLocalPolicies();
        for (ApiAccessPolicy policy : remotePolicies) {
            policies.put(policy.getApiIdentifier(), policy);
        }
    }

    private void loadLocalPolicies() {
        properties.getApiPolicies().forEach((identifier, policy) -> {
            policy.setApiIdentifier(identifier);
            if (policy.getServiceName() == null) {
                policy.setServiceName(properties.getServiceName());
            }
            policies.put(identifier, policy);
        });
    }
}
