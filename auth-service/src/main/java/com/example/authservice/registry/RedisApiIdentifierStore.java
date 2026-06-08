package com.example.authservice.registry;

import java.util.Collection;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.example.authservice.auth.AuthProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@ConditionalOnProperty(prefix = "auth.api-registry", name = "storage", havingValue = "redis", matchIfMissing = true)
public class RedisApiIdentifierStore implements ApiIdentifierStore {
    private final AuthProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisApiIdentifierStore(
            AuthProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Collection<ApiIdentifierRecord> findAll() {
        return redisTemplate.opsForHash().entries(redisKey()).values().stream()
                .map(this::read)
                .toList();
    }

    @Override
    public Optional<ApiIdentifierRecord> findByIdentifier(String apiIdentifier) {
        Object value = redisTemplate.opsForHash().get(redisKey(), apiIdentifier);
        return Optional.ofNullable(value).map(this::read);
    }

    @Override
    public void save(ApiIdentifierRecord record) {
        redisTemplate.opsForHash().put(redisKey(), record.getApiIdentifier(), write(record));
    }

    private String redisKey() {
        return properties.getApiRegistry().getRedisKey();
    }

    private String write(ApiIdentifierRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize API identifier record", ex);
        }
    }

    private ApiIdentifierRecord read(Object value) {
        try {
            return objectMapper.readValue(value.toString(), ApiIdentifierRecord.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize API identifier record", ex);
        }
    }
}
