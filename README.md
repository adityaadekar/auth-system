# auth-system

Reference Spring Boot JWT issuance and authorization system for store users.

## Modules

- `auth-service`: owns JWT issuance for users that were already authenticated by an external auth service, key publication, Redis-backed API identifier registry, actor values, and API-to-actor mappings. It does not verify OTP/password credentials or create local login sessions.
- `authz-starter`: reusable Spring Boot starter/library for microservices. It provides `@Authenticate("API_IDENTIFIER")`, validates JWTs offline using JWKS, checks local API identifier policy cache, can refresh that cache from Redis policy-change events, and exposes the authenticated principal without defining actor types.
- `example-service`: minimal protected service showing how another microservice integrates the starter.

See [Auth and Authorization Design](docs/auth-authorization-design.md) for the full ownership model, portal-backed policy flow, refresh scenarios, and operational guidance.

## JWT issuance flow

1. The frontend or trusted edge service obtains and validates an access/session token from the external auth service.
2. A trusted caller posts the already-authenticated session and user context to `POST /auth/jwt`:

   ```json
   {
     "sessionToken": "external-session-token-123",
     "applicationId": "store-pos-web",
     "store": {
       "storeId": "store-001",
       "storeCode": "BLR-KRM",
       "name": "Koramangala Flagship",
       "city": "Bengaluru",
       "region": "South",
       "attributes": {
         "format": "FLAGSHIP",
         "inventoryZone": "BLR-SOUTH"
       }
     },
     "salesman": {
       "salesmanId": "S1001",
       "displayName": "Aarav Sales",
       "actorType": "SALESMAN",
       "attributes": {
         "employeeCode": "EMP-S-1001",
         "counter": "C3"
       }
     },
     "actorGroups": ["STORE_STAFF"]
   }
   ```

3. `auth-service` does not authenticate the user or look up the profile. It signs the supplied context into a JWT and returns:

   ```json
   {
     "tokenType": "Bearer",
     "jwtToken": "<signed-jwt>",
     "expiresAt": "2026-06-08T09:07:00Z"
   }
   ```

   The JWT contains:
   - the upstream session token/reference as `sid`
   - application id as `app_id`
   - actor type and actor groups from the request
   - store and salesman details from the request

   Actor groups are included in JWTs as `actor_groups` so APIs can authorize a group without the shared starter knowing the actor catalog.

4. Frontend sends this JWT to microservices using `Authorization: Bearer <jwt>`.

The external auth service remains responsible for authenticating the user, validating or revoking the upstream access/session token, and supplying trusted identity context to the JWT issuance endpoint.

## Authorization in microservices

Add the starter dependency:

```xml
<dependency>
  <groupId>com.example</groupId>
  <artifactId>authz-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Annotate only APIs that need authentication:

```java
@GetMapping("/orders")
@Authenticate("STORE_ORDERS_READ")
public List<Order> orders() {
    AuthenticatedPrincipal principal = RequestAuthContextHolder.requireCurrent();
    return orderService.findForStore(principal.store().storeId());
}
```

If `@Authenticate` is absent, the starter does not authenticate or authorize that endpoint.

Consuming microservices should not define actor mappings in code or `authz` configuration. The starter supplies auth-service, JWKS, registry, auto-registration, revocation cache, and service-name defaults; the annotation only carries the API identifier. Actor mappings live in auth-service persistent storage and are managed through the portal.

If `@Authenticate` is present:

1. Missing/invalid/revoked JWT returns `401`.
2. Unknown `API_IDENTIFIER` returns `401`.
3. Known identifier without a configured actor mapping returns `403`.
4. Known identifier but disallowed actor type/group returns `403`.
5. Store and salesman details come only from JWT claims produced by the JWT issuance response; the microservice does not look them up in its database.

The starter auto-registers annotated API identifiers on application startup at `POST /internal/api-identifiers`. Auto-registration discovers service/path/method metadata; it does not grant access until auth-service has a policy mapping for the identifier.

## API identifier registry

`auth-service` exposes:

- `POST /internal/api-identifiers`: register identifiers, service name, paths, HTTP methods, and optional allowed actor types/groups.
- `GET /internal/api-identifiers?serviceName=order-service`: policy view consumed by microservices.
- `GET /internal/api-identifiers/records`: operational view including paths and methods.

This registry is backed by Redis by default and should be managed through the auth portal in production. For tests or a no-Redis local run, set `AUTH_API_REGISTRY_STORAGE=memory`.

## JWT authorization without DB lookup

Microservices validate JWT signature and expiry using the auth service JWKS endpoint. Authorization data needed for the request is in the token:

- `sid`: upstream session token/reference supplied by the external auth service
- `app_id`: frontend application id
- `actor_type`: actor value supplied by the trusted JWT issuance caller
- `actor_groups`: groups supplied by the trusted JWT issuance caller
- `store`: store details
- `salesman`: salesman details

API identifier policies are cached locally by the starter. This means normal requests do not perform database lookups. The cache refreshes from the registry service periodically and can also refresh immediately from Redis policy-change events when `authz.policy-events.enabled=true`.

## Key storage in Vault

Production signing keys should be generated and stored in Vault, not in source control.

Recommended layout:

- `secret/auth/jwt/current/private_key_pem`
- `secret/auth/jwt/current/public_key_pem`
- `secret/auth/jwt/current/key_id`

`auth-service` is the only service that needs the private key. It reads the private key, public key, and key id from Vault at startup via Spring Cloud Vault, Vault Agent template rendering, or environment injection into:

```yaml
auth:
  jwt:
    ttl: 30m
    key-id: ${JWT_KEY_ID}
    private-key-pem: ${JWT_PRIVATE_KEY_PEM}
    public-key-pem: ${JWT_PUBLIC_KEY_PEM}
```

Microservices do not need the private key or local authz config. They use the starter defaults to fetch public keys through `/.well-known/jwks.json` and cache them through the starter's Nimbus verifier.

For key rotation:

1. Write a new key pair to Vault with a new `key_id`.
2. Deploy/reload `auth-service` so new JWTs are signed with the new key.
3. Publish both old and new public keys until all old JWTs expire.
4. Remove the old public key after the max JWT lifetime has passed.

## JWT invalidation and logout

Pure self-contained JWT validation cannot instantly invalidate tokens without checking some external state. There are three practical options:

1. Short JWT TTL: simplest and fully offline, but revocation waits until expiry.
2. Distributed revocation cache: the external auth/session service publishes revoked upstream session ids/JWT ids; microservices keep an in-memory cache and check it on each request. This repository keeps the starter cache shape through `GET /internal/revocations`, which currently returns an empty list until integrated with an external revocation source.
3. Emergency key rotation: invalidates a broad set of tokens signed by a key, useful for incidents but disruptive.

For logout and role/store-assignment changes, revoke the user's upstream session in the external auth service and stop minting new JWTs with the old context. This auth service no longer exposes local logout, OTP verification, or session invalidation endpoints.

In production, revocation publication should use Redis, Kafka, or another shared low-latency channel instead of periodic polling. The request path still avoids DB lookups; it checks only local cryptographic validation and in-memory policy/revocation caches.

## Local demo

Run:

```bash
redis-server
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl example-service -Dspring-boot.run.arguments=--server.port=8081
```

If Redis is not available for a quick local run, start auth-service with `AUTH_ACTOR_CATALOG_STORAGE=memory AUTH_API_REGISTRY_STORAGE=memory`.

Then issue a JWT for an already-authenticated external session:

```bash
curl -X POST http://localhost:8080/auth/jwt \
  -H 'Content-Type: application/json' \
  -d '{
    "sessionToken": "external-session-token-123",
    "applicationId": "store-pos-web",
    "store": {
      "storeId": "store-001",
      "storeCode": "BLR-KRM",
      "name": "Koramangala Flagship",
      "city": "Bengaluru",
      "region": "South",
      "attributes": {
        "format": "FLAGSHIP",
        "inventoryZone": "BLR-SOUTH"
      }
    },
    "salesman": {
      "salesmanId": "S1001",
      "displayName": "Aarav Sales",
      "actorType": "SALESMAN",
      "attributes": {
        "employeeCode": "EMP-S-1001",
        "counter": "C3"
      }
    },
    "actorGroups": ["STORE_STAFF"]
  }'
```

Call `GET http://localhost:8081/orders` with `Authorization: Bearer <jwtToken>` from the response.
