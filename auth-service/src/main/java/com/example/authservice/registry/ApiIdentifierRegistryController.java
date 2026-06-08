package com.example.authservice.registry;

import java.util.Collection;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.authservice.session.RevocationRepository;
import com.example.authservice.session.SessionRecord;
import com.example.authservice.session.SessionService;
import com.example.authz.ApiAccessPolicy;
import com.example.authz.ApiIdentifierRegistration;
import com.example.authz.RevokedToken;

@RestController
public class ApiIdentifierRegistryController {
    private final ApiIdentifierRegistryService registryService;
    private final RevocationRepository revocationRepository;
    private final SessionService sessionService;

    public ApiIdentifierRegistryController(
            ApiIdentifierRegistryService registryService,
            RevocationRepository revocationRepository,
            SessionService sessionService
    ) {
        this.registryService = registryService;
        this.revocationRepository = revocationRepository;
        this.sessionService = sessionService;
    }

    @GetMapping("/internal/api-identifiers")
    public List<ApiAccessPolicy> policies(@RequestParam(name = "serviceName", required = false) String serviceName) {
        return registryService.policies(serviceName);
    }

    @GetMapping("/internal/api-identifiers/records")
    public List<ApiIdentifierRecord> records(@RequestParam(name = "serviceName", required = false) String serviceName) {
        return registryService.records(serviceName);
    }

    @PostMapping("/internal/api-identifiers")
    public Collection<ApiIdentifierRecord> register(@RequestBody Collection<ApiIdentifierRegistration> registrations) {
        return registryService.register(registrations);
    }

    @GetMapping("/internal/revocations")
    public Collection<RevokedToken> revocations(@RequestParam(name = "serviceName", required = false) String serviceName) {
        return revocationRepository.findAll();
    }

    @PostMapping("/internal/users/{salesmanId}/invalidate")
    public Collection<SessionRecord> invalidateSalesmanSessions(@PathVariable("salesmanId") String salesmanId) {
        return sessionService.invalidateSalesmanSessions(salesmanId);
    }
}
