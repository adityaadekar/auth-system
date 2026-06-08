package com.example.authservice.registry;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.example.authservice.auth.AuthProperties;

@Component
@ConditionalOnProperty(prefix = "auth.api-registry", name = "storage", havingValue = "redis", matchIfMissing = true)
public class RedisApiIdentifierChangePublisher implements ApiIdentifierChangePublisher {
    private final AuthProperties properties;
    private final StringRedisTemplate redisTemplate;

    public RedisApiIdentifierChangePublisher(AuthProperties properties, StringRedisTemplate redisTemplate) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void publishPolicyChanged(String apiIdentifier) {
        redisTemplate.convertAndSend(properties.getApiRegistry().getPolicyChangeChannel(), apiIdentifier);
    }
}
