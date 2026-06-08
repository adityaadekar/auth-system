package com.example.authz;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

public class ApiDescriptorScanner implements ApplicationListener<ApplicationReadyEvent> {
    private final AuthzProperties properties;
    private final ApiIdentifierRegistryClient registryClient;
    private final Collection<RequestMappingInfoHandlerMapping> handlerMappings;

    public ApiDescriptorScanner(
            AuthzProperties properties,
            ApiIdentifierRegistryClient registryClient,
            Collection<RequestMappingInfoHandlerMapping> handlerMappings
    ) {
        this.properties = properties;
        this.registryClient = registryClient;
        this.handlerMappings = handlerMappings;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (!properties.isAutoRegisterApis()) {
            return;
        }
        registryClient.register(scan());
    }

    private Collection<ApiIdentifierRegistration> scan() {
        Map<String, ApiIdentifierRegistration> registrations = new LinkedHashMap<>();
        for (RequestMappingInfoHandlerMapping handlerMapping : handlerMappings) {
            handlerMapping.getHandlerMethods().forEach((mappingInfo, handlerMethod) -> {
                Authenticate authenticate = resolveAuthenticate(handlerMethod);
                if (authenticate == null) {
                    return;
                }
                ApiIdentifierRegistration registration = registrations.computeIfAbsent(authenticate.value(), this::newRegistration);
                registration.getPathPatterns().addAll(pathPatterns(mappingInfo));
                mappingInfo.getMethodsCondition().getMethods().forEach(method -> registration.getHttpMethods().add(method.name()));
            });
        }
        return registrations.values();
    }

    private ApiIdentifierRegistration newRegistration(String apiIdentifier) {
        ApiIdentifierRegistration registration = new ApiIdentifierRegistration();
        registration.setServiceName(properties.getServiceName());
        registration.setApiIdentifier(apiIdentifier);
        ApiAccessPolicy configuredPolicy = properties.getApiPolicies().get(apiIdentifier);
        if (configuredPolicy != null) {
            registration.setAllowedActorTypes(configuredPolicy.getAllowedActorTypes());
            registration.setAllowedActorGroups(configuredPolicy.getAllowedActorGroups());
        }
        return registration;
    }

    private Set<String> pathPatterns(RequestMappingInfo mappingInfo) {
        Set<String> patterns = new LinkedHashSet<>();
        if (mappingInfo.getPathPatternsCondition() != null) {
            mappingInfo.getPathPatternsCondition().getPatternValues().forEach(patterns::add);
        }
        if (mappingInfo.getPatternsCondition() != null) {
            patterns.addAll(mappingInfo.getPatternsCondition().getPatterns());
        }
        return patterns;
    }

    private Authenticate resolveAuthenticate(HandlerMethod handlerMethod) {
        Authenticate methodAnnotation = AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getMethod(), Authenticate.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }
        return AnnotatedElementUtils.findMergedAnnotation(handlerMethod.getBeanType(), Authenticate.class);
    }
}
