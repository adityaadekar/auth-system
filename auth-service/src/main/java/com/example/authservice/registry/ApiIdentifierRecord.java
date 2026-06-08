package com.example.authservice.registry;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

import com.example.authz.ApiAccessPolicy;
import com.example.authz.ApiIdentifierRegistration;

public class ApiIdentifierRecord {
    private String serviceName;
    private String apiIdentifier;
    private Set<String> pathPatterns = new LinkedHashSet<>();
    private Set<String> httpMethods = new LinkedHashSet<>();
    private Set<String> allowedActorTypes = new LinkedHashSet<>();
    private Set<String> allowedActorGroups = new LinkedHashSet<>();
    private boolean active = true;
    private Instant updatedAt = Instant.now();

    public static ApiIdentifierRecord fromRegistration(ApiIdentifierRegistration registration) {
        ApiIdentifierRecord record = new ApiIdentifierRecord();
        record.serviceName = registration.getServiceName();
        record.apiIdentifier = registration.getApiIdentifier();
        record.pathPatterns = registration.getPathPatterns();
        record.httpMethods = registration.getHttpMethods();
        record.allowedActorTypes = registration.getAllowedActorTypes();
        record.allowedActorGroups = registration.getAllowedActorGroups();
        record.updatedAt = Instant.now();
        return record;
    }

    public ApiAccessPolicy toPolicy() {
        ApiAccessPolicy policy = new ApiAccessPolicy();
        policy.setServiceName(serviceName);
        policy.setApiIdentifier(apiIdentifier);
        policy.setAllowedActorTypes(allowedActorTypes);
        policy.setAllowedActorGroups(allowedActorGroups);
        policy.setActive(active);
        return policy;
    }

    public void merge(ApiIdentifierRegistration registration) {
        serviceName = registration.getServiceName();
        pathPatterns = registration.getPathPatterns();
        httpMethods = registration.getHttpMethods();
        if (!registration.getAllowedActorTypes().isEmpty() || !registration.getAllowedActorGroups().isEmpty()) {
            allowedActorTypes = registration.getAllowedActorTypes();
            allowedActorGroups = registration.getAllowedActorGroups();
        }
        updatedAt = Instant.now();
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getApiIdentifier() {
        return apiIdentifier;
    }

    public Set<String> getPathPatterns() {
        return pathPatterns;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public Set<String> getAllowedActorTypes() {
        return allowedActorTypes;
    }

    public Set<String> getAllowedActorGroups() {
        return allowedActorGroups;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
