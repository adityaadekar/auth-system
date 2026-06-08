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

}
