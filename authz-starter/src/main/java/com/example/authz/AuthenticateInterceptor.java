package com.example.authz;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthenticateInterceptor implements HandlerInterceptor {
    public static final String PRINCIPAL_REQUEST_ATTRIBUTE = AuthenticatedPrincipal.class.getName();

    private final JwtTokenVerifier tokenVerifier;
    private final ApiIdentifierCache apiIdentifierCache;
    private final JwtRevocationCache revocationCache;

    public AuthenticateInterceptor(
            JwtTokenVerifier tokenVerifier,
            ApiIdentifierCache apiIdentifierCache,
            JwtRevocationCache revocationCache
    ) {
        this.tokenVerifier = tokenVerifier;
        this.apiIdentifierCache = apiIdentifierCache;
        this.revocationCache = revocationCache;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Authenticate authenticate = resolveAuthenticate(handlerMethod);
        if (authenticate == null) {
            return true;
        }

        String token = bearerToken(request);
        if (token == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "missing_bearer_token");
            return false;
        }

        AuthenticatedPrincipal principal;
        try {
            principal = tokenVerifier.verify(token);
        } catch (JwtAuthenticationException ex) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "invalid_token");
            return false;
        }

        if (revocationCache.isRevoked(principal)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "revoked_token");
            return false;
        }

        ApiAccessPolicy policy = apiIdentifierCache.find(authenticate.value()).orElse(null);
        if (policy == null) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "unknown_api_identifier");
            return false;
        }
        if (!policy.allows(principal)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "insufficient_actor_type");
            return false;
        }

        request.setAttribute(PRINCIPAL_REQUEST_ATTRIBUTE, principal);
        RequestAuthContextHolder.set(principal);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        RequestAuthContextHolder.clear();
    }

    private Authenticate resolveAuthenticate(HandlerMethod handlerMethod) {
        Authenticate methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Authenticate.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Authenticate.class);
    }

    private String bearerToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return null;
        }
        return authorization.substring("Bearer ".length()).trim();
    }

    private void writeError(HttpServletResponse response, int status, String code) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}
