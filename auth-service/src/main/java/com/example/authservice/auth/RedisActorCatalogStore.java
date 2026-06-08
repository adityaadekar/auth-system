package com.example.authservice.auth;

import java.util.Collection;
import java.util.Optional;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
@ConditionalOnProperty(prefix = "auth.actor-catalog", name = "storage", havingValue = "redis", matchIfMissing = true)
public class RedisActorCatalogStore implements ActorCatalogStore {
    private final AuthProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public RedisActorCatalogStore(
            AuthProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<ActorTypeRecord> findByType(String actorType) {
        Object value = redisTemplate.opsForHash().get(redisKey(), actorType);
        return Optional.ofNullable(value).map(this::read);
    }

    @Override
    public void save(ActorTypeRecord actorType) {
        redisTemplate.opsForHash().put(redisKey(), actorType.getActorType(), write(actorType));
    }

    @Override
    public long count() {
        return redisTemplate.opsForHash().size(redisKey());
    }

    @Override
    public Collection<ActorTypeRecord> findAll() {
        return redisTemplate.opsForHash().entries(redisKey()).values().stream()
                .map(this::read)
                .toList();
    }

    private String redisKey() {
        return properties.getActorCatalog().getRedisKey();
    }

    private String write(ActorTypeRecord actorType) {
        try {
            return objectMapper.writeValueAsString(actorType);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to serialize actor catalog record", ex);
        }
    }

    private ActorTypeRecord read(Object value) {
        try {
            return objectMapper.readValue(value.toString(), ActorTypeRecord.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Unable to deserialize actor catalog record", ex);
        }
    }
}
