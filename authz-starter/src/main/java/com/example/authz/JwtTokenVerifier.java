package com.example.authz;

import java.net.MalformedURLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.DefaultResourceRetriever;

public class JwtTokenVerifier {
    private final AuthzProperties properties;
    private final Clock clock;
    private final ConfigurableJWTProcessor<SecurityContext> jwtProcessor;

    public JwtTokenVerifier(AuthzProperties properties, Clock clock) {
        this.properties = properties;
        this.clock = clock;
        this.jwtProcessor = buildProcessor(properties);
    }

    public AuthenticatedPrincipal verify(String token) {
        try {
            JWTClaimsSet claims = jwtProcessor.process(token, null);
            validateClaims(claims);
            return JwtPrincipalConverter.convert(claims);
        } catch (JwtAuthenticationException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new JwtAuthenticationException("JWT validation failed", ex);
        }
    }

    private ConfigurableJWTProcessor<SecurityContext> buildProcessor(AuthzProperties authzProperties) {
        if (authzProperties.getJwkSetUri() == null) {
            throw new IllegalStateException("authz.jwk-set-uri is required when authz is enabled");
        }
        try {
            DefaultJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            DefaultResourceRetriever retriever = new DefaultResourceRetriever(2000, 2000);
            JWKSource<SecurityContext> jwkSource = new RemoteJWKSet<>(authzProperties.getJwkSetUri().toURL(), retriever);
            JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, jwkSource);
            processor.setJWSKeySelector(keySelector);
            return processor;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("authz.jwk-set-uri must be a valid URL", ex);
        }
    }

    private void validateClaims(JWTClaimsSet claims) {
        Instant now = clock.instant();
        Date expiresAt = claims.getExpirationTime();
        if (expiresAt == null || !expiresAt.toInstant().isAfter(now)) {
            throw new JwtAuthenticationException("JWT has expired");
        }
        Date notBefore = claims.getNotBeforeTime();
        if (notBefore != null && notBefore.toInstant().isAfter(now)) {
            throw new JwtAuthenticationException("JWT is not valid yet");
        }
        if (properties.getIssuer() != null && !properties.getIssuer().toString().equals(claims.getIssuer())) {
            throw new JwtAuthenticationException("JWT issuer is not trusted");
        }
    }
}
