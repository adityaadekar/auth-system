package com.example.authservice.auth;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.authservice.keys.JwtKeyPairProvider;
import com.example.authz.SalesmanContext;
import com.example.authz.StoreContext;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@Service
public class JwtIssuer {
    private final AuthProperties properties;
    private final JwtKeyPairProvider keyPairProvider;
    private final Clock clock;

    public JwtIssuer(
            AuthProperties properties,
            JwtKeyPairProvider keyPairProvider,
            Clock clock
    ) {
        this.properties = properties;
        this.keyPairProvider = keyPairProvider;
        this.clock = clock;
    }

    public JwtIssueResponse issue(JwtIssueRequest request) {
        Instant now = clock.instant();
        Instant expiresAt = now.plus(properties.getJwt().getTtl());
        StoreContext store = request.storeContext();
        SalesmanContext salesman = request.salesmanContext();
        try {
            String actorType = salesman.actorType();
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer(properties.getIssuer().toString())
                    .subject(salesman.salesmanId())
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim("sid", request.sessionToken())
                    .claim("app_id", request.applicationId())
                    .claim("actor_type", actorType)
                    .claim("actor_groups", request.normalizedActorGroups())
                    .claim("store", storeClaims(store))
                    .claim("salesman", salesmanClaims(salesman))
                    .build();

            SignedJWT signedJwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(keyPairProvider.keyId()).build(),
                    claims
            );
            signedJwt.sign(new RSASSASigner(keyPairProvider.privateKey()));
            return new JwtIssueResponse("Bearer", signedJwt.serialize(), expiresAt);
        } catch (JOSEException | DateTimeException ex) {
            throw new IllegalStateException("Unable to issue JWT", ex);
        }
    }

    private Map<String, Object> storeClaims(StoreContext store) {
        Map<String, Object> claims = new LinkedHashMap<>(store.attributes());
        claims.put("storeId", store.storeId());
        claims.put("storeCode", store.storeCode());
        claims.put("name", store.name());
        claims.put("city", store.city());
        claims.put("region", store.region());
        return claims;
    }

    private Map<String, Object> salesmanClaims(SalesmanContext salesman) {
        Map<String, Object> claims = new LinkedHashMap<>(salesman.attributes());
        claims.put("salesmanId", salesman.salesmanId());
        claims.put("displayName", salesman.displayName());
        claims.put("actorType", salesman.actorType());
        return claims;
    }

}
