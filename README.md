# auth-system

Reference Spring Boot authentication and authorization system for store users.

## Modules

- `auth-service`: owns OTP authentication, opaque session tokens, JWT issuance, key publication, API identifier registry, actor values, API-to-actor mappings, and revocation publication.
- `authz-starter`: reusable Spring Boot starter/library for microservices. It provides `@Authenticate("API_IDENTIFIER")`, validates JWTs offline using JWKS, checks local API identifier policy cache, and exposes the authenticated principal without defining actor types.
- `example-service`: minimal protected service showing how another microservice integrates the starter.

See [Auth and Authorization Design](docs/auth-authorization-design.md) for the full ownership model, portal-backed policy flow, refresh scenarios, and operational guidance.

## Authentication flow

1. Frontend calls `POST /auth/otp/verify` with:

   ```json
   {
     "salesmanId": "S1001",
     "otp": "123456",
     "applicationId": "store-pos-web",
     "deviceId": "browser-or-device-id"
   }
   ```

2. `auth-service` validates `salesmanId + otp`, resolves the user's assigned store and salesman profile, creates a session, and returns:
   - store details
   - salesman details
   - actor type from the auth-service actor catalog
   - opaque session token
   - signed JWT
   - expiry

   Actor groups are calculated by auth-service and included in JWTs as `actor_groups` so APIs can authorize a group without the shared starter knowing the actor catalog.

3. Frontend sends this JWT to microservices using `Authorization: Bearer <jwt>`.

Each frontend app login creates a separate session (`applicationId` is stored on the session). The OTP verification response includes a JWT with that same session id and expiry, so a user can be logged in separately on multiple apps without an extra frontend exchange call.

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
5. Store and salesman details come only from JWT claims produced by the auth response; the microservice does not look them up in its database.

The starter auto-registers annotated API identifiers on application startup at `POST /internal/api-identifiers`. Auto-registration discovers service/path/method metadata; it does not grant access until auth-service has a policy mapping for the identifier.

## API identifier registry

`auth-service` exposes:

- `POST /internal/api-identifiers`: register identifiers, service name, paths, HTTP methods, and optional allowed actor types/groups.
- `GET /internal/api-identifiers?serviceName=order-service`: policy view consumed by microservices.
- `GET /internal/api-identifiers/records`: operational view including paths and methods.

This registry should be backed by persistent storage in production and managed through the auth portal. This repository uses an in-memory implementation to keep the example self-contained.

## JWT authorization without DB lookup

Microservices validate JWT signature and expiry using the auth service JWKS endpoint. Authorization data needed for the request is in the token:

- `sid`: session id
- `app_id`: frontend application id
- `actor_type`: actor value issued by auth-service
- `actor_groups`: groups issued by auth-service
- `store`: store details
- `salesman`: salesman details

API identifier policies are cached locally by the starter. This means normal requests do not perform database lookups. The cache can be refreshed from the registry service periodically and, in production, should also react to policy-change events.

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
2. Distributed revocation cache: auth-service publishes revoked session ids/JWT ids; microservices keep an in-memory cache and check it on each request. This repository implements the cache shape through `GET /internal/revocations` and the starter's `JwtRevocationCache`.
3. Emergency key rotation: invalidates a broad set of tokens signed by a key, useful for incidents but disruptive.

For changes to store admin/salesman/optometrist/usher/repair roles or assigned store, call:

```http
POST /internal/users/{salesmanId}/invalidate
```

That revokes active sessions for the user and publishes their session ids. Logout uses:

```http
DELETE /auth/sessions/{sessionToken}
```

In production, revocation publication should use Redis, Kafka, or another shared low-latency channel instead of periodic polling. The request path still avoids DB lookups; it checks only local cryptographic validation and in-memory policy/revocation caches.

## Local demo

Seeded users all use OTP `123456`:

- `S1001`: `SALESMAN`
- `A1001`: `STORE_ADMIN`
- `O1001`: `REMOTE_OPTOM` with `OPTOMETRIST` group
- `D1001`: `DISPENSING_OPTOM` with `OPTOMETRIST` group

Run:

```bash
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl example-service -Dspring-boot.run.arguments=--server.port=8081
```

Then authenticate and call `GET http://localhost:8081/orders` with the JWT returned by OTP verification.
