package com.example.authservice.registry;

import java.util.Collection;
import java.util.Optional;

public interface ApiIdentifierStore {
    Collection<ApiIdentifierRecord> findAll();

    Optional<ApiIdentifierRecord> findByIdentifier(String apiIdentifier);

    void save(ApiIdentifierRecord record);
}
