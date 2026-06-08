package com.example.authservice.session;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {
    private final ConcurrentMap<String, SessionRecord> byToken = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, String> tokenBySessionId = new ConcurrentHashMap<>();

    public SessionRecord save(SessionRecord session) {
        byToken.put(session.sessionToken(), session);
        tokenBySessionId.put(session.sessionId(), session.sessionToken());
        return session;
    }

    public Optional<SessionRecord> findActiveByToken(String sessionToken, Instant now) {
        return Optional.ofNullable(byToken.get(sessionToken))
                .filter(session -> !session.revoked())
                .filter(session -> !session.isExpired(now));
    }

    public Collection<SessionRecord> findActiveBySalesmanId(String salesmanId, Instant now) {
        return byToken.values().stream()
                .filter(session -> session.salesman().salesmanId().equals(salesmanId))
                .filter(session -> !session.revoked())
                .filter(session -> !session.isExpired(now))
                .toList();
    }

    public Optional<SessionRecord> revokeByToken(String sessionToken) {
        SessionRecord current = byToken.get(sessionToken);
        if (current == null || current.revoked()) {
            return Optional.empty();
        }
        SessionRecord revoked = current.revoke();
        byToken.put(sessionToken, revoked);
        return Optional.of(revoked);
    }

    public Optional<SessionRecord> revokeBySessionId(String sessionId) {
        String token = tokenBySessionId.get(sessionId);
        if (token == null) {
            return Optional.empty();
        }
        return revokeByToken(token);
    }
}
