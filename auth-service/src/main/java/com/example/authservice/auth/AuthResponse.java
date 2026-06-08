package com.example.authservice.auth;

import java.time.Instant;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

public record AuthResponse(
        StoreContext store,
        SalesmanContext salesman,
        String sessionToken,
        String jwtToken,
        Instant expiresAt
) {
}
