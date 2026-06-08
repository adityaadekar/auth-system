package com.example.authz;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "authz")
public class AuthzProperties {
    private boolean enabled = true;
    private String serviceName = "unknown-service";
    private URI issuer;
    private URI jwkSetUri;
    private URI registryUri;
    private boolean autoRegisterApis = true;
    private Duration registryRefreshInterval = Duration.ofMinutes(1);
    private Map<String, ApiAccessPolicy> apiPolicies = new LinkedHashMap<>();

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

    public Map<String, ApiAccessPolicy> getApiPolicies() {
        return apiPolicies;
    }

    public void setApiPolicies(Map<String, ApiAccessPolicy> apiPolicies) {
        this.apiPolicies = apiPolicies == null ? new LinkedHashMap<>() : apiPolicies;
    }
}
