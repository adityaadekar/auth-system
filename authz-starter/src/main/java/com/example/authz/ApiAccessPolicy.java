package com.example.authz;

import java.util.LinkedHashSet;
import java.util.Set;

public class ApiAccessPolicy {
    private String serviceName;
    private String apiIdentifier;
    private Set<ActorType> allowedActorTypes = new LinkedHashSet<>();
    private Set<String> allowedActorGroups = new LinkedHashSet<>();
    private boolean active = true;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getApiIdentifier() {
        return apiIdentifier;
    }

    public void setApiIdentifier(String apiIdentifier) {
        this.apiIdentifier = apiIdentifier;
    }

    public Set<ActorType> getAllowedActorTypes() {
        return allowedActorTypes;
    }

    public void setAllowedActorTypes(Set<ActorType> allowedActorTypes) {
        this.allowedActorTypes = allowedActorTypes == null ? new LinkedHashSet<>() : allowedActorTypes;
    }

    public Set<String> getAllowedActorGroups() {
        return allowedActorGroups;
    }

    public void setAllowedActorGroups(Set<String> allowedActorGroups) {
        this.allowedActorGroups = allowedActorGroups == null ? new LinkedHashSet<>() : allowedActorGroups;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean allows(AuthenticatedPrincipal principal) {
        if (!active) {
            return false;
        }
        if (allowedActorTypes.isEmpty() && allowedActorGroups.isEmpty()) {
            return true;
        }
        if (allowedActorTypes.contains(principal.actorType())) {
            return true;
        }
        return principal.actorGroups().stream().anyMatch(allowedActorGroups::contains);
    }
}
