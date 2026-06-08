package com.example.authservice.registry;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.example.authz.ApiAccessPolicy;
import com.example.authz.ApiIdentifierRegistration;

@Service
public class ApiIdentifierRegistryService {
    private final ConcurrentMap<String, ApiIdentifierRecord> records = new ConcurrentHashMap<>();

    public List<ApiAccessPolicy> policies(String serviceName) {
        return records.values().stream()
                .filter(record -> serviceName == null || record.getServiceName().equals(serviceName))
                .map(ApiIdentifierRecord::toPolicy)
                .toList();
    }

    public List<ApiIdentifierRecord> records(String serviceName) {
        return records.values().stream()
                .filter(record -> serviceName == null || record.getServiceName().equals(serviceName))
                .toList();
    }

    public Collection<ApiIdentifierRecord> register(Collection<ApiIdentifierRegistration> registrations) {
        for (ApiIdentifierRegistration registration : registrations) {
            records.compute(registration.getApiIdentifier(), (identifier, current) -> {
                if (current == null) {
                    return ApiIdentifierRecord.fromRegistration(registration);
                }
                current.merge(registration);
                return current;
            });
        }
        return records.values();
    }
}
