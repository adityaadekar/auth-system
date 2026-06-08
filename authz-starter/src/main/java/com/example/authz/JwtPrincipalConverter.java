package com.example.authz;

import java.text.ParseException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import com.nimbusds.jwt.JWTClaimsSet;

final class JwtPrincipalConverter {
    private JwtPrincipalConverter() {
    }

    static AuthenticatedPrincipal convert(JWTClaimsSet claims) {
        try {
            ActorType actorType = ActorType.valueOf(claims.getStringClaim("actor_type"));
            Set<String> actorGroups = new LinkedHashSet<>(claims.getStringListClaim("actor_groups"));
            StoreContext store = toStoreContext(claims.getJSONObjectClaim("store"));
            SalesmanContext salesman = toSalesmanContext(claims.getJSONObjectClaim("salesman"), actorType);
            Instant expiresAt = claims.getExpirationTime().toInstant();

            return new AuthenticatedPrincipal(
                    claims.getSubject(),
                    claims.getStringClaim("sid"),
                    claims.getStringClaim("app_id"),
                    claims.getJWTID(),
                    store,
                    salesman,
                    actorType,
                    actorGroups,
                    expiresAt
            );
        } catch (IllegalArgumentException | ParseException ex) {
            throw new JwtAuthenticationException("JWT does not contain the required authentication claims", ex);
        }
    }

    private static StoreContext toStoreContext(Map<String, Object> value) {
        Map<String, Object> attributes = mutableAttributes(value);
        return new StoreContext(
                asString(attributes.remove("storeId")),
                asString(attributes.remove("storeCode")),
                asString(attributes.remove("name")),
                asString(attributes.remove("city")),
                asString(attributes.remove("region")),
                Map.copyOf(attributes)
        );
    }

    private static SalesmanContext toSalesmanContext(Map<String, Object> value, ActorType actorType) {
        Map<String, Object> attributes = mutableAttributes(value);
        return new SalesmanContext(
                asString(attributes.remove("salesmanId")),
                asString(attributes.remove("displayName")),
                actorType,
                Map.copyOf(attributes)
        );
    }

    private static Map<String, Object> mutableAttributes(Map<String, Object> value) {
        if (value == null) {
            return new LinkedHashMap<>();
        }
        return new LinkedHashMap<>(value);
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
