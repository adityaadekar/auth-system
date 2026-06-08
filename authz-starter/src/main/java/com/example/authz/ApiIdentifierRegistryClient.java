package com.example.authz;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

public class ApiIdentifierRegistryClient {
    private final AuthzProperties properties;
    private final RestClient restClient;

    public ApiIdentifierRegistryClient(AuthzProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.build();
    }

    public Optional<List<ApiAccessPolicy>> fetchPolicies() {
        if (properties.getRegistryUri() == null) {
            return Optional.empty();
        }
        URI uri = UriComponentsBuilder.fromUri(properties.getRegistryUri())
                .path("/internal/api-identifiers")
                .queryParam("serviceName", properties.getServiceName())
                .build()
                .toUri();
        try {
            ApiAccessPolicy[] policies = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(ApiAccessPolicy[].class);
            return Optional.of(policies == null ? List.of() : Arrays.asList(policies));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    public Optional<List<RevokedToken>> fetchRevocations() {
        if (properties.getRegistryUri() == null) {
            return Optional.empty();
        }
        URI uri = UriComponentsBuilder.fromUri(properties.getRegistryUri())
                .path("/internal/revocations")
                .queryParam("serviceName", properties.getServiceName())
                .build()
                .toUri();
        try {
            RevokedToken[] revokedTokens = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(RevokedToken[].class);
            return Optional.of(revokedTokens == null ? List.of() : Arrays.asList(revokedTokens));
        } catch (RestClientException ex) {
            return Optional.empty();
        }
    }

    public void register(Collection<ApiIdentifierRegistration> registrations) {
        if (properties.getRegistryUri() == null || registrations.isEmpty()) {
            return;
        }
        URI uri = UriComponentsBuilder.fromUri(properties.getRegistryUri())
                .path("/internal/api-identifiers")
                .build()
                .toUri();
        try {
            restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(registrations)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException ex) {
            // Keep application startup independent from registry availability.
        }
    }
}
