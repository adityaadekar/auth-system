package com.example.authservice.session;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.authservice.auth.AuthProperties;
import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final RevocationRepository revocationRepository;
    private final AuthProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public SessionService(
            SessionRepository sessionRepository,
            RevocationRepository revocationRepository,
            AuthProperties properties,
            Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.revocationRepository = revocationRepository;
        this.properties = properties;
        this.clock = clock;
    }

    public SessionRecord create(String applicationId, String deviceId, StoreContext store, SalesmanContext salesman) {
        Instant now = clock.instant();
        SessionRecord session = new SessionRecord(
                UUID.randomUUID().toString(),
                newOpaqueToken(),
                applicationId,
                deviceId,
                store,
                salesman,
                now.plus(properties.getSessionTtl()),
                now,
                false
        );
        return sessionRepository.save(session);
    }

    public Optional<SessionRecord> findActive(String sessionToken) {
        return sessionRepository.findActiveByToken(sessionToken, clock.instant());
    }

    public Optional<SessionRecord> logout(String sessionToken) {
        Optional<SessionRecord> revoked = sessionRepository.revokeByToken(sessionToken);
        revoked.ifPresent(session -> revocationRepository.revokeSession(session.sessionId()));
        return revoked;
    }

    public Collection<SessionRecord> invalidateSalesmanSessions(String salesmanId) {
        Collection<SessionRecord> activeSessions = sessionRepository.findActiveBySalesmanId(salesmanId, clock.instant());
        for (SessionRecord session : activeSessions) {
            sessionRepository.revokeBySessionId(session.sessionId());
            revocationRepository.revokeSession(session.sessionId());
        }
        return activeSessions;
    }

    private String newOpaqueToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
