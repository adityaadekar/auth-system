package com.example.authservice.auth;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "auth.actor-catalog", name = "storage", havingValue = "memory")
public class InMemoryActorCatalogStore implements ActorCatalogStore {
    private final ConcurrentMap<String, ActorTypeRecord> actors = new ConcurrentHashMap<>();

    @Override
    public Optional<ActorTypeRecord> findByType(String actorType) {
        return Optional.ofNullable(actors.get(actorType));
    }

    @Override
    public void save(ActorTypeRecord actorType) {
        actors.put(actorType.getActorType(), actorType);
    }

    @Override
    public long count() {
        return actors.size();
    }

    @Override
    public Collection<ActorTypeRecord> findAll() {
        return actors.values();
    }
}
