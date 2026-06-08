package com.example.authz;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "authz")
public class AuthzProperties {
    private static final URI DEFAULT_AUTH_SERVICE_URI = URI.create("http://localhost:8080");

    private boolean enabled = true;
    private String serviceName;
    private URI issuer = DEFAULT_AUTH_SERVICE_URI;
    private URI jwkSetUri = DEFAULT_AUTH_SERVICE_URI.resolve("/.well-known/jwks.json");
    private URI registryUri = DEFAULT_AUTH_SERVICE_URI;
    private boolean autoRegisterApis = true;
    private Duration registryRefreshInterval = Duration.ofMinutes(1);
    private PolicyEvents policyEvents = new PolicyEvents();
    private Redis redis = new Redis();

    public void applyEnvironmentDefaults(Environment environment) {
        if (!StringUtils.hasText(serviceName)) {
            serviceName = environment.getProperty("spring.application.name", "application");
        }
        if (jwkSetUri == null && issuer != null) {
            jwkSetUri = issuer.resolve("/.well-known/jwks.json");
        }
        if (registryUri == null) {
            registryUri = issuer;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public URI getIssuer() {
        return issuer;
    }

    public void setIssuer(URI issuer) {
        this.issuer = issuer;
    }

    public URI getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(URI jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public URI getRegistryUri() {
        return registryUri;
    }

    public void setRegistryUri(URI registryUri) {
        this.registryUri = registryUri;
    }

    public boolean isAutoRegisterApis() {
        return autoRegisterApis;
    }

    public void setAutoRegisterApis(boolean autoRegisterApis) {
        this.autoRegisterApis = autoRegisterApis;
    }

    public Duration getRegistryRefreshInterval() {
        return registryRefreshInterval;
    }

    public void setRegistryRefreshInterval(Duration registryRefreshInterval) {
        this.registryRefreshInterval = registryRefreshInterval;
    }

    public PolicyEvents getPolicyEvents() {
        return policyEvents;
    }

    public void setPolicyEvents(PolicyEvents policyEvents) {
        this.policyEvents = policyEvents;
    }

    public Redis getRedis() {
        return redis;
    }

    public void setRedis(Redis redis) {
        this.redis = redis;
    }

    public static class PolicyEvents {
        private boolean enabled;
        private String channel = "auth:policy-changes";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }
    }

    public static class Redis {
        private String host = "localhost";
        private int port = 6379;
        private String username;
        private String password;
        private int database;
        private boolean ssl;
        private Duration timeout = Duration.ofSeconds(2);

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public int getDatabase() {
            return database;
        }

        public void setDatabase(int database) {
            this.database = database;
        }

        public boolean isSsl() {
            return ssl;
        }

        public void setSsl(boolean ssl) {
            this.ssl = ssl;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public void setTimeout(Duration timeout) {
            this.timeout = timeout;
        }
    }

}
