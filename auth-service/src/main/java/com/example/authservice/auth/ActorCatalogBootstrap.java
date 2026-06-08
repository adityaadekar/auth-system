package com.example.authservice.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ActorCatalogBootstrap implements CommandLineRunner {
    private final AuthProperties properties;
    private final ActorCatalogService actorCatalogService;

    public ActorCatalogBootstrap(AuthProperties properties, ActorCatalogService actorCatalogService) {
        this.properties = properties;
        this.actorCatalogService = actorCatalogService;
    }

    @Override
    public void run(String... args) {
        if (properties.getActorCatalog().isBootstrapEnabled()) {
            actorCatalogService.bootstrapIfEmpty(properties.getActorCatalog().getBootstrap());
        }
    }
}
