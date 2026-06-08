package com.example.authservice.registry;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.authz.ApiAccessPolicy;
import com.example.authz.ApiIdentifierRegistration;

@Service
public class ApiIdentifierRegistryService {
    private final ApiIdentifierStore store;
    private final ApiIdentifierChangePublisher changePublisher;

    public ApiIdentifierRegistryService(ApiIdentifierStore store, ApiIdentifierChangePublisher changePublisher) {
        this.store = store;
        this.changePublisher = changePublisher;
    }

    public List<ApiAccessPolicy> policies(String serviceName) {
        return store.findAll().stream()
                .filter(record -> serviceName == null || record.getServiceName().equals(serviceName))
                .map(ApiIdentifierRecord::toPolicy)
                .toList();
    }

    public List<ApiIdentifierRecord> records(String serviceName) {
        return store.findAll().stream()
                .filter(record -> serviceName == null || record.getServiceName().equals(serviceName))
                .toList();
    }

    public Collection<ApiIdentifierRecord> register(Collection<ApiIdentifierRegistration> registrations) {
        for (ApiIdentifierRegistration registration : registrations) {
            ApiIdentifierRecord record = store.findByIdentifier(registration.getApiIdentifier())
                    .map(current -> {
                        current.merge(registration);
                        return current;
                    })
                    .orElseGet(() -> ApiIdentifierRecord.fromRegistration(registration));
            store.save(record);
            changePublisher.publishPolicyChanged(record.getApiIdentifier());
        }
        return store.findAll();
    }
}
