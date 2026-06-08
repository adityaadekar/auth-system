package com.example.authservice.registry;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(prefix = "auth.api-registry", name = "storage", havingValue = "memory")
public class InMemoryApiIdentifierStore implements ApiIdentifierStore {
    private final ConcurrentMap<String, ApiIdentifierRecord> records = new ConcurrentHashMap<>();

    @Override
    public Collection<ApiIdentifierRecord> findAll() {
        return records.values();
    }

    @Override
    public Optional<ApiIdentifierRecord> findByIdentifier(String apiIdentifier) {
        return Optional.ofNullable(records.get(apiIdentifier));
    }

    @Override
    public void save(ApiIdentifierRecord record) {
        records.put(record.getApiIdentifier(), record);
    }
}
