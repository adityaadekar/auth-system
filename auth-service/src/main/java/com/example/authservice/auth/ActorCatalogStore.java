package com.example.authservice.auth;

import java.util.Collection;
import java.util.Optional;

public interface ActorCatalogStore {
    Optional<ActorTypeRecord> findByType(String actorType);

    void save(ActorTypeRecord actorType);

    long count();

    Collection<ActorTypeRecord> findAll();
}
