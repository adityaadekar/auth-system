# Auth and Authorization Design

This document explains the intended auth design for the store platform. It is written as a reference for explaining the model to engineering, product, security, and operations teams.

## Goals

- Keep actor and role values out of the shared/common authorization library.
- Make `auth-service` the source of truth for:
  - actor values such as `SALESMAN`, `STORE_ADMIN`, optometrist variants, and future roles;
  - actor groups;
  - API identifiers;
  - mappings between API identifiers and allowed actors/groups;
  - user/session state;
  - JWT signing keys and token creation.
- Let a portal manage API identifiers, actors, and mappings through UI.
- Store portal-managed configuration in persistent storage.
- Let microservices authorize requests without a database lookup on every request.
- Avoid exposing a public JWT creation/exchange API.

## Short version

1. A microservice marks protected endpoints with only an API identifier:

   ```java
   @GetMapping("/orders")
   @Authenticate("STORE_ORDERS_READ")
   public OrderResponse orders() {
       ...
   }
   ```

2. The shared starter validates the JWT and asks its local cache whether the current actor is allowed for `STORE_ORDERS_READ`.
3. The local cache is populated from `auth-service`.
4. `auth-service` gets the policy from persistent storage that is managed by the portal.
5. Actor names and mappings are not compiled into the common starter or consuming microservices.

## Component responsibilities

### auth-service

`auth-service` owns authentication, authorization configuration, session state, and token issuance.

Responsibilities:

- Verify OTP/login credentials.
- Resolve the user's current store, salesman profile, actor type, actor groups, and other claims.
- Create and revoke sessions.
- Issue signed JWTs after successful authentication.
- Publish public signing keys through JWKS.
- Store API identifiers and access mappings in persistent storage.
- Serve internal policy APIs consumed by microservices.
- Serve portal APIs used by admins to configure actors and mappings.
- Publish or expose revocation data so microservices can reject revoked sessions.

`auth-service` is the only service that should know the complete actor catalog. If a new actor is added, it is added to auth-service data/configuration and portal records, not to the common library.

### Authorization starter/common library

The shared starter is intentionally generic.

Responsibilities:

- Provide `@Authenticate("API_IDENTIFIER")`.
- Extract and validate `Authorization: Bearer <jwt>`.
- Validate JWT signature, issuer, not-before, and expiry using auth-service JWKS.
- Convert JWT claims into a generic authenticated principal.
- Read `actor_type` and `actor_groups` as strings.
- Cache API access policies fetched from auth-service.
- Check the request actor against the cached policy.
- Check a local revocation cache.
- Auto-register discovered API identifiers and endpoint metadata with auth-service.

Non-responsibilities:

- It must not define enum values for actors.
- It must not decide which actors exist.
- It must not keep permanent API-to-actor mappings in microservice configuration.
- It must not create JWTs.

### Microservices

Microservices own business APIs. They should not own auth configuration.

Responsibilities:

- Add the starter dependency.
- Add `@Authenticate("API_IDENTIFIER")` to endpoints that require auth.
- Use `RequestAuthContextHolder.requireCurrent()` when business logic needs the authenticated user's store/salesman context.
- Treat `actorType` and `actorGroups` as data from auth-service, not as locally compiled enums.

Microservice code should not say:

```java
@Authenticate(
    value = "STORE_ORDERS_READ",
    allowedActorTypes = {ActorType.STORE_ADMIN, ActorType.SALESMAN}
)
```

Microservice code should say:

```java
@Authenticate("STORE_ORDERS_READ")
```

The mapping from `STORE_ORDERS_READ` to `STORE_ADMIN`, `SALESMAN`, or any other actor belongs in auth-service persistent storage and is managed through the portal.

### Portal

The portal is the operational UI for access management.

It should allow authorized administrators to:

- register API identifiers;
- see which service and paths use each identifier;
- define actors and actor groups;
- map API identifiers to allowed actors/groups;
- activate or deactivate API identifiers;
- review audit history for permission changes;
- trigger policy cache refresh if an immediate rollout is required;
- invalidate sessions for a user after role or store assignment changes.

## Persistent storage model

The exact database schema can evolve, but the following logical records are needed.

### Actor catalog

Stores actor values owned by auth-service.

Example fields:

- `actor_type`: `SALESMAN`, `STORE_ADMIN`, `REMOTE_OPTOM`, etc.
- `display_name`
- `description`
- `active`
- `created_at`
- `updated_at`

### Actor groups

Stores higher-level groups such as `OPTOMETRIST`.

Example fields:

- `actor_group`
- `display_name`
- `description`
- `active`

### Actor-to-group mapping

Stores memberships, for example:

- `REMOTE_OPTOM` -> `OPTOMETRIST`
- `DISPENSING_OPTOM` -> `OPTOMETRIST`
- `KIDS_OPTOM` -> `OPTOMETRIST`

### API identifier registry

Stores protected API identifiers and endpoint metadata.

Example fields:

- `api_identifier`: `STORE_ORDERS_READ`
- `service_name`: `order-service`
- `path_patterns`: `/orders`
- `http_methods`: `GET`
- `active`
- `description`
- `created_at`
- `updated_at`

API identifiers can be auto-discovered by microservices at startup, then reviewed and configured through the portal.

### API access policy

Stores the mapping from API identifiers to actors/groups.

Example fields:

- `api_identifier`
- `allowed_actor_types`
- `allowed_actor_groups`
- `active`
- `version`
- `updated_at`
- `updated_by`

If both `allowed_actor_types` and `allowed_actor_groups` are empty, the API is not configured and should deny access. Do not treat an empty mapping as "allow everyone".

### Sessions and revocations

Stores active user sessions and revoked sessions.

Example fields:

- `session_id`
- `session_token_hash`
- `salesman_id`
- `application_id`
- `device_id`
- `store_id`
- `actor_type_at_login`
- `actor_groups_at_login`
- `expires_at`
- `revoked_at`
- `revocation_reason`

JWTs are short-lived and self-contained. Session/revocation state is used to force logout before token expiry when required.

## Authentication flow

1. Frontend sends OTP verification request to auth-service:

   ```http
   POST /auth/otp/verify
   ```

2. auth-service verifies the OTP.
3. auth-service loads the user's current store, salesman profile, actor type, and groups from its source of truth.
4. auth-service creates a session.
5. auth-service issues a signed JWT.
6. Frontend stores the opaque session token and JWT.
7. Frontend calls microservices with:

   ```http
   Authorization: Bearer <jwt>
   ```

The JWT contains claims such as:

- `sid`: session id
- `sub`: salesman/user id
- `app_id`: frontend application id
- `actor_type`: string actor value from auth-service
- `actor_groups`: string groups from auth-service
- `store`: store context needed by downstream services
- `salesman`: salesman context needed by downstream services
- `exp`: token expiry
- `jti`: JWT id

## Authorization flow

1. Request reaches a microservice endpoint.
2. The starter checks whether the endpoint has `@Authenticate`.
3. If the annotation is absent, the starter does nothing.
4. If the annotation is present:
   - missing JWT returns `401`;
   - invalid/expired JWT returns `401`;
   - revoked session/JWT returns `401`;
   - unknown API identifier returns `401`;
   - known identifier with no actor mapping returns `403`;
   - known identifier with actor mismatch returns `403`;
   - matching actor type/group allows the request.

The microservice does not call a database on the request path. It validates the JWT offline and checks in-memory policy/revocation caches.

## JWT creation API

Do not expose a public "create JWT" or "exchange session for JWT" API.

Allowed token creation path:

- user completes an authentication ceremony such as OTP verification;
- auth-service creates the session and JWT internally;
- auth-service returns the JWT as part of the successful login response.

Not allowed:

- a public endpoint that accepts a session token and returns a new JWT;
- a public endpoint that accepts user details and mints a JWT;
- any microservice issuing JWTs.

If server-side components ever need token minting for a backend flow, make it an internal auth-service capability protected by service authentication, network policy, audit logging, and narrow scopes.

## Policy refresh when API permissions change

API permission changes are configuration changes. They should not require users to logout.

Example: `STORE_ORDERS_READ` was allowed for `STORE_ADMIN` and `SALESMAN`; now it should also allow `OPTOMETRIST`.

Recommended flow:

1. Admin updates the mapping in the portal.
2. Portal writes the new mapping to auth-service persistent storage.
3. auth-service increments the policy version and records an audit entry.
4. Microservices refresh their policy cache.
5. New requests use the updated policy.

Refresh options:

- Periodic polling: each microservice pulls policies every configured interval, for example every minute.
- Push invalidation: auth-service publishes a policy-changed event through Redis, Kafka, SNS/SQS, or another bus; microservices refresh immediately.
- Manual refresh: portal exposes an operational action to trigger refresh for urgent changes.

Recommended production behavior:

- Use push invalidation for quick propagation.
- Keep periodic polling as a safety net.
- Include `version` or `updated_at` in policy responses so microservices can avoid unnecessary reloads.
- Continue serving the last known good policy if auth-service is temporarily unavailable.
- Do not allow an auto-registered API with no portal mapping by default.

### Scenario: permission added

Before:

- `STORE_ORDERS_READ` allows `STORE_ADMIN`.

Change:

- Portal adds `SALESMAN`.

Result:

- Existing JWTs for salesmen do not need to change because the token already says `actor_type = SALESMAN`.
- Once the microservice policy cache refreshes, salesmen can access the API.

### Scenario: permission removed

Before:

- `STORE_DISCOUNT_APPROVE` allows `STORE_ADMIN` and `SALESMAN`.

Change:

- Portal removes `SALESMAN`.

Result:

- Existing JWTs do not need to change.
- Once the microservice policy cache refreshes, salesman requests get `403`.
- No logout is required because the user's role did not change; only the API mapping changed.

## Role refresh when a salesman's role changes

Role changes are identity/session changes. A JWT already issued to the user contains the old `actor_type` and `actor_groups` until it expires or is revoked.

Example: salesman `S1001` changes from `SALESMAN` to `STORE_ADMIN`.

Recommended flow:

1. Admin updates the user's role in the auth portal or source user-management system.
2. auth-service persists the new role.
3. auth-service invalidates active sessions for that user.
4. auth-service publishes revoked session ids/JWT ids.
5. Microservices refresh or receive revocation data.
6. Existing requests with old JWTs return `401 revoked_token`.
7. User is asked to logout and login again.
8. New login creates a new session and JWT with the updated `actor_type` and `actor_groups`.

This is the cleanest model because the JWT remains self-contained and microservices do not need to call auth-service or a database on every request to check whether the user's role changed.

### Why ask the salesman to logout/login?

The JWT is a signed snapshot of the user's auth context at login time. If the user's role changes after the JWT is issued, downstream services cannot safely infer the new role from the old token.

Forcing logout/login gives the user:

- a new session;
- a new JWT;
- updated actor claims;
- updated store/salesman context.

### Scenario: role promoted

Before:

- `S1001` has `actor_type = SALESMAN`.

Change:

- Admin changes `S1001` to `STORE_ADMIN`.

Result:

- Old sessions are revoked.
- Old JWTs are rejected after revocation reaches microservices.
- User logs in again.
- New JWT has `actor_type = STORE_ADMIN`.

### Scenario: role downgraded

Before:

- `S1001` has `actor_type = STORE_ADMIN`.

Change:

- Admin changes `S1001` to `SALESMAN`.

Result:

- This is security sensitive because old JWTs may still contain elevated privileges.
- auth-service should immediately revoke active sessions.
- Microservices should receive revocation quickly through push invalidation and periodic polling.
- User must login again to get a lower-privilege token.

### Scenario: store assignment changed

Before:

- `S1001` is assigned to `store-001`.

Change:

- `S1001` moves to `store-002`.

Result:

- Treat this like a role/context change.
- Revoke active sessions.
- Ask user to login again.
- New JWT carries the new store context.

## Revocation strategy

JWT validation is normally offline, so revocation requires a small amount of shared state.

Recommended approach:

- JWT TTL should be short.
- auth-service records revoked session ids and/or JWT ids.
- auth-service publishes revocation events.
- Microservices keep an in-memory revocation cache.
- Microservices also poll auth-service periodically as a fallback.

Request-time behavior:

- Validate JWT signature and expiry.
- Read `sid` and `jti`.
- Check local revocation cache.
- If revoked, return `401`.

This keeps the request path fast and avoids database lookups.

## API identifier lifecycle

### 1. Developer adds an API

Developer annotates the endpoint:

```java
@Authenticate("STORE_INVOICE_READ")
```

### 2. Service starts

The starter scans annotated endpoints and posts metadata to auth-service:

```http
POST /internal/api-identifiers
```

Metadata includes:

- service name;
- API identifier;
- path pattern;
- HTTP method.

### 3. Portal review

The portal shows the new identifier as discovered but not fully configured.

### 4. Admin maps actors

Admin chooses allowed actor types/groups.

Example:

- actor types: `STORE_ADMIN`
- actor groups: `OPTOMETRIST`

### 5. Policy is published

auth-service persists the mapping and publishes a policy refresh event.

### 6. Microservices enforce

Microservices refresh their cache and enforce the new mapping.

## Internal API protection

Endpoints under `/internal/**` should not be public internet APIs.

Protect them with:

- private network or service mesh policy;
- service-to-service authentication;
- authorization for caller service identity;
- audit logging;
- rate limiting where appropriate.

Internal endpoints include:

- API identifier registration;
- API policy fetch;
- revocation fetch;
- user/session invalidation;
- cache refresh hooks.

## Failure behavior

### auth-service unavailable during normal requests

Microservices should continue using their last known good policy and JWKS cache until TTLs require refresh. This keeps business APIs resilient.

If a microservice starts with no policy cache and cannot reach auth-service, protected APIs should fail closed.

### New API identifier without portal mapping

The identifier may be discovered automatically, but it should deny access until an admin configures actors/groups in the portal.

### Policy cache stale

Policy changes become effective after cache refresh. Use push invalidation plus periodic polling to reduce stale windows.

### Revocation cache stale

Role downgrades and user disablement are more sensitive than permission additions. Use push revocation events and short JWT TTLs to reduce risk.

## Response code guidelines

- `401 missing_bearer_token`: no bearer token.
- `401 invalid_token`: bad signature, expired token, wrong issuer, or malformed claims.
- `401 revoked_token`: session/JWT was revoked.
- `401 unknown_api_identifier`: endpoint identifier is not known to the policy cache.
- `403 access_policy_not_configured`: identifier exists but no actor mapping is configured.
- `403 insufficient_actor_type`: authenticated actor is not allowed for this API.

## Operational checklist

Before production:

- Move the in-memory API registry to persistent storage.
- Move sessions/revocations to persistent storage or a distributed session store.
- Add portal screens for actors, groups, API identifiers, and mappings.
- Add audit logs for all policy and role changes.
- Protect `/internal/**` endpoints with service authentication and network policy.
- Use short JWT TTLs.
- Add push events for policy and revocation changes.
- Keep periodic polling as fallback.
- Monitor policy refresh failures and revocation lag.
- Keep JWT private signing keys only in auth-service, preferably loaded from Vault/KMS.

## Summary for explanation

The common starter is a generic enforcement mechanism. It does not know business roles. Microservices only declare "this endpoint is API identifier X." auth-service and the portal decide which actors can call X. API permission changes refresh through policy cache updates and do not require logout. User role or store assignment changes require session invalidation and logout/login because JWTs contain a signed snapshot of the user's role at login time. JWT creation stays inside auth-service and is not exposed as a public token minting API.
