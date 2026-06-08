package com.example.authservice.auth;

import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ActorCatalogService {
    private final ActorCatalogStore actorCatalogStore;
    private final Clock clock;

    public ActorCatalogService(ActorCatalogStore actorCatalogStore, Clock clock) {
        this.actorCatalogStore = actorCatalogStore;
        this.clock = clock;
    }

    public ActorCatalogResolution resolve(String actorType, Set<String> suppliedGroups) {
        String normalizedActorType = normalize(actorType);
        ActorTypeRecord record = actorCatalogStore.findByType(normalizedActorType)
                .filter(ActorTypeRecord::isActive)
                .orElseThrow(() -> new UnknownActorTypeException(actorType));

        Set<String> groups = new LinkedHashSet<>(record.getGroups());
        if (suppliedGroups != null) {
            groups.addAll(suppliedGroups);
        }
        return new ActorCatalogResolution(record.getActorType(), groups);
    }

    public void bootstrapIfEmpty(Iterable<ActorTypeRecord> bootstrapActors) {
        if (actorCatalogStore.count() > 0) {
            return;
        }
        Instant now = clock.instant();
        for (ActorTypeRecord actor : bootstrapActors) {
            String normalizedActorType = normalize(actor.getActorType());
            actor.setActorType(normalizedActorType);
            if (actor.getCreatedAt() == null) {
                actor.setCreatedAt(now);
            }
            actor.setUpdatedAt(now);
            actorCatalogStore.save(actor);
        }
    }

    private String normalize(String actorType) {
        if (!StringUtils.hasText(actorType)) {
            throw new UnknownActorTypeException(actorType);
        }
        return actorType.trim().toUpperCase(Locale.ROOT);
    }

    public record ActorCatalogResolution(String actorType, Set<String> actorGroups) {
    }
}
