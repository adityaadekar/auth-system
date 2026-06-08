package com.example.authservice.auth;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
    private URI issuer = URI.create("http://localhost:8080");
    private Jwt jwt = new Jwt();
    private ActorCatalog actorCatalog = new ActorCatalog();
    private ApiRegistry apiRegistry = new ApiRegistry();

    public URI getIssuer() {
        return issuer;
    }

    public void setIssuer(URI issuer) {
        this.issuer = issuer;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public ActorCatalog getActorCatalog() {
        return actorCatalog;
    }

    public void setActorCatalog(ActorCatalog actorCatalog) {
        this.actorCatalog = actorCatalog;
    }

    public ApiRegistry getApiRegistry() {
        return apiRegistry;
    }

    public void setApiRegistry(ApiRegistry apiRegistry) {
        this.apiRegistry = apiRegistry;
    }

    public static class Jwt {
        private Duration ttl = Duration.ofMinutes(30);
        private String keyId = "local-dev-key";
        private String privateKeyPem;
        private String publicKeyPem;

        public Duration getTtl() {
            return ttl;
        }

        public void setTtl(Duration ttl) {
            this.ttl = ttl;
        }

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

    public static class ActorCatalog {
        private String storage = "redis";
        private String redisKey = "auth:actor-types";
        private boolean bootstrapEnabled = true;
        private List<ActorTypeRecord> bootstrap = new ArrayList<>();

        public String getStorage() {
            return storage;
        }

        public void setStorage(String storage) {
            this.storage = storage;
        }

        public String getRedisKey() {
            return redisKey;
        }

        public void setRedisKey(String redisKey) {
            this.redisKey = redisKey;
        }

        public boolean isBootstrapEnabled() {
            return bootstrapEnabled;
        }

        public void setBootstrapEnabled(boolean bootstrapEnabled) {
            this.bootstrapEnabled = bootstrapEnabled;
        }

        public List<ActorTypeRecord> getBootstrap() {
            return bootstrap;
        }

        public void setBootstrap(List<ActorTypeRecord> bootstrap) {
            this.bootstrap = bootstrap == null ? new ArrayList<>() : bootstrap;
        }
    }

    public static class ApiRegistry {
        private String storage = "redis";
        private String redisKey = "auth:api-identifiers";
        private String policyChangeChannel = "auth:policy-changes";

        public String getStorage() {
            return storage;
        }

        public void setStorage(String storage) {
            this.storage = storage;
        }

        public String getRedisKey() {
            return redisKey;
        }

        public void setRedisKey(String redisKey) {
            this.redisKey = redisKey;
        }

        public String getPolicyChangeChannel() {
            return policyChangeChannel;
        }

        public void setPolicyChangeChannel(String policyChangeChannel) {
            this.policyChangeChannel = policyChangeChannel;
        }
    }
}
