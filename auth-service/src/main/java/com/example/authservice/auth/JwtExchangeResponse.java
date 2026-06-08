package com.example.authservice.auth;

import java.time.Instant;

import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;

public record JwtExchangeResponse(
        StoreContext store,
        SalesmanContext salesman,
        String jwtToken,
        Instant expiresAt
) {
}
