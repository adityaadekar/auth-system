package com.example.authz;

import java.util.Optional;

public final class RequestAuthContextHolder {
    private static final ThreadLocal<AuthenticatedPrincipal> CURRENT = new ThreadLocal<>();

    private RequestAuthContextHolder() {
    }

    static void set(AuthenticatedPrincipal principal) {
        CURRENT.set(principal);
    }

    static void clear() {
        CURRENT.remove();
    }

    public static Optional<AuthenticatedPrincipal> current() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static AuthenticatedPrincipal requireCurrent() {
        return current().orElseThrow(() -> new IllegalStateException("No authenticated principal is bound to this request"));
    }
}
