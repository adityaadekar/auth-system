package com.example.authservice.auth;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private URI issuer = URI.create("http://localhost:8080");
    private Duration sessionTtl = Duration.ofMinutes(30);
    private Jwt jwt = new Jwt();

    public URI getIssuer() {
        return issuer;
    }

    public void setIssuer(URI issuer) {
        this.issuer = issuer;
    }

    public Duration getSessionTtl() {
        return sessionTtl;
    }

    public void setSessionTtl(Duration sessionTtl) {
        this.sessionTtl = sessionTtl;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public static class Jwt {
        private String keyId = "local-dev-key";
        private String privateKeyPem;
        private String publicKeyPem;

        public String getKeyId() {
            return keyId;
        }

        public void setKeyId(String keyId) {
            this.keyId = keyId;
        }

        public String getPrivateKeyPem() {
            return privateKeyPem;
        }

        public void setPrivateKeyPem(String privateKeyPem) {
            this.privateKeyPem = privateKeyPem;
        }

        public String getPublicKeyPem() {
            return publicKeyPem;
        }

        public void setPublicKeyPem(String publicKeyPem) {
            this.publicKeyPem = publicKeyPem;
        }
    }
}
