package com.example.authservice.auth;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class ActorTypeRecord {
    private String actorType;
    private String displayName;
    private String description;
    private boolean active = true;
    private Set<String> groups = new LinkedHashSet<>();
    private Instant createdAt;
    private Instant updatedAt;

    public String getActorType() {
        return actorType;
    }

    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Set<String> getGroups() {
        return groups;
    }

    public void setGroups(Set<String> groups) {
        this.groups = groups == null ? new LinkedHashSet<>() : new LinkedHashSet<>(groups);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
