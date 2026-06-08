package com.example.authservice.auth;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record JwtIssueRequest(
        @NotBlank String sessionToken,
        @NotBlank String applicationId,
        @Valid @NotNull StoreClaims store,
        @Valid @NotNull SalesmanClaims salesman,
        Set<@NotBlank String> actorGroups
) {
    public StoreContext storeContext() {
        return new StoreContext(
                store.storeId(),
                store.storeCode(),
                store.name(),
                store.city(),
                store.region(),
                copyAttributes(store.attributes())
        );
    }

    public SalesmanContext salesmanContext() {
        return new SalesmanContext(
                salesman.salesmanId(),
                salesman.displayName(),
                salesman.actorType(),
                copyAttributes(salesman.attributes())
        );
    }

    public Set<String> normalizedActorGroups() {
        return actorGroups == null ? Set.of() : Set.copyOf(actorGroups);
    }

    private static Map<String, Object> copyAttributes(Map<String, Object> attributes) {
        return attributes == null ? Map.of() : new LinkedHashMap<>(attributes);
    }

    public record StoreClaims(
            @NotBlank String storeId,
            @NotBlank String storeCode,
            String name,
            String city,
            String region,
            Map<String, Object> attributes
    ) {
    }

    public record SalesmanClaims(
            @NotBlank String salesmanId,
            @NotBlank String displayName,
            @NotBlank String actorType,
            Map<String, Object> attributes
    ) {
    }
}
