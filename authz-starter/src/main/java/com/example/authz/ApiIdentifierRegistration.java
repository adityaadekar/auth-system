package com.example.authz;

import java.util.LinkedHashSet;
import java.util.Set;

public class ApiIdentifierRegistration {
    private String serviceName;
    private String apiIdentifier;
    private Set<String> pathPatterns = new LinkedHashSet<>();
    private Set<String> httpMethods = new LinkedHashSet<>();
    private Set<ActorType> allowedActorTypes = new LinkedHashSet<>();
    private Set<String> allowedActorGroups = new LinkedHashSet<>();

    public ApiAccessPolicy toPolicy() {
        ApiAccessPolicy policy = new ApiAccessPolicy();
        policy.setServiceName(serviceName);
        policy.setApiIdentifier(apiIdentifier);
        policy.setAllowedActorTypes(allowedActorTypes);
        policy.setAllowedActorGroups(allowedActorGroups);
        return policy;
    }

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

    public Set<String> getPathPatterns() {
        return pathPatterns;
    }

    public void setPathPatterns(Set<String> pathPatterns) {
        this.pathPatterns = pathPatterns == null ? new LinkedHashSet<>() : pathPatterns;
    }

    public Set<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(Set<String> httpMethods) {
        this.httpMethods = httpMethods == null ? new LinkedHashSet<>() : httpMethods;
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
}
