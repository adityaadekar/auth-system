package com.example.authz;

import java.time.Clock;
import java.util.Collection;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
    ApiIdentifierRegistryClient apiIdentifierRegistryClient(AuthzProperties properties, RestClient.Builder builder) {
        return new ApiIdentifierRegistryClient(properties, builder);
    }

    @Bean
    @ConditionalOnMissingBean
    ApiIdentifierCache apiIdentifierCache(AuthzProperties properties, ApiIdentifierRegistryClient client, Clock clock) {
        return new ApiIdentifierCache(properties, client, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtRevocationCache jwtRevocationCache(AuthzProperties properties, ApiIdentifierRegistryClient client, Clock clock) {
        return new JwtRevocationCache(properties, client, clock);
    }

    @Bean
    @ConditionalOnMissingBean
    JwtTokenVerifier jwtTokenVerifier(AuthzProperties properties, Clock clock) {
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
    WebMvcConfigurer authzWebMvcConfigurer(AuthzProperties properties, AuthenticateInterceptor authenticateInterceptor) {
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
            ApiIdentifierRegistryClient registryClient,
            Collection<RequestMappingInfoHandlerMapping> handlerMappings
    ) {
        return new ApiDescriptorScanner(properties, registryClient, handlerMappings);
    }
}
