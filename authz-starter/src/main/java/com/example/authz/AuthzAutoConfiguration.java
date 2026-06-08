package com.example.authz;

import java.time.Clock;
import java.util.Collection;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(prefix = "authz", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(AuthzProperties.class)
public class AuthzAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    Clock authzClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    RestClient.Builder authzRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @ConditionalOnMissingBean
    ApiIdentifierRegistryClient apiIdentifierRegistryClient(
            AuthzProperties properties,
            Environment environment,
            RestClient.Builder builder
    ) {
        properties.applyEnvironmentDefaults(environment);
        return new ApiIdentifierRegistryClient(properties, builder);
    }

    @Bean
    @ConditionalOnMissingBean
    ApiIdentifierCache apiIdentifierCache(
            AuthzProperties properties,
            Environment environment,
            ApiIdentifierRegistryClient client,
            Clock clock
    ) {
        properties.applyEnvironmentDefaults(environment);
        return new ApiIdentifierCache(properties, client, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtRevocationCache jwtRevocationCache(
            AuthzProperties properties,
            Environment environment,
            ApiIdentifierRegistryClient client,
            Clock clock
    ) {
        properties.applyEnvironmentDefaults(environment);
        return new JwtRevocationCache(properties, client, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtTokenVerifier jwtTokenVerifier(AuthzProperties properties, Environment environment, Clock clock) {
        properties.applyEnvironmentDefaults(environment);
        return new JwtTokenVerifier(properties, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    AuthenticateInterceptor authenticateInterceptor(
            JwtTokenVerifier tokenVerifier,
            ApiIdentifierCache apiIdentifierCache,
            JwtRevocationCache revocationCache
    ) {
        return new AuthenticateInterceptor(tokenVerifier, apiIdentifierCache, revocationCache);
    }

    @Bean
    WebMvcConfigurer authzWebMvcConfigurer(
            AuthzProperties properties,
            Environment environment,
            AuthenticateInterceptor authenticateInterceptor
    ) {
        properties.applyEnvironmentDefaults(environment);
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                if (properties.isEnabled()) {
                    registry.addInterceptor(authenticateInterceptor).order(-100);
                }
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    ApiDescriptorScanner apiDescriptorScanner(
            AuthzProperties properties,
            Environment environment,
            ApiIdentifierRegistryClient registryClient,
            ApiIdentifierCache apiIdentifierCache,
            Collection<RequestMappingInfoHandlerMapping> handlerMappings
    ) {
        properties.applyEnvironmentDefaults(environment);
        return new ApiDescriptorScanner(properties, registryClient, apiIdentifierCache, handlerMappings);
    }
}
