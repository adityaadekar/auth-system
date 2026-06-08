package com.example.authservice.session;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

import com.example.authz.RevokedToken;

@Repository
public class RevocationRepository {
    private final Map<String, RevokedToken> revokedBySessionId = new ConcurrentHashMap<>();

    public void revokeSession(String sessionId) {
        revokedBySessionId.put(sessionId, new RevokedToken(sessionId, null, Instant.now()));
    }

    public Collection<RevokedToken> findAll() {
        return revokedBySessionId.values().stream()
                .sorted(Comparator.comparing(RevokedToken::revokedAt))
                .toList();
    }
}
