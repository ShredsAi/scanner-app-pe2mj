package ai.shreds.infrastructure.repositories;

import ai.shreds.domain.ports.DomainOutputPortCacheRepository;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionRedisException;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Redis implementation of the cache repository port.
 * Provides caching operations using Redis as the underlying storage.
 */
@Repository
public class InfrastructureRedisCacheRepositoryImpl implements DomainOutputPortCacheRepository {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureRedisCacheRepositoryImpl.class);
    private static final String CACHE_KEY_PREFIX = "scanner:";
    
    private final RedisTemplate<String, String> redisTemplate;
    private final SharedUtilJsonSerializer jsonSerializer;

    public InfrastructureRedisCacheRepositoryImpl(RedisTemplate<String, String> redisTemplate, 
                                                 SharedUtilJsonSerializer jsonSerializer) {
        this.redisTemplate = redisTemplate;
        this.jsonSerializer = jsonSerializer;
    }

    @Override
    public void save(String key, String value, Duration ttl) {
        try {
            String cacheKey = generateCacheKey(key);
            redisTemplate.opsForValue().set(cacheKey, value, ttl);
            logger.debug("Saved cache entry with key: {} and TTL: {}", cacheKey, ttl);
        } catch (Exception e) {
            handleRedisException(e, "save", key);
        }
    }

    @Override
    public Optional<String> get(String key) {
        try {
            String cacheKey = generateCacheKey(key);
            String value = redisTemplate.opsForValue().get(cacheKey);
            logger.debug("Retrieved cache entry for key: {}, found: {}", cacheKey, value != null);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            handleRedisException(e, "get", key);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            String cacheKey = generateCacheKey(key);
            Boolean deleted = redisTemplate.delete(cacheKey);
            logger.debug("Deleted cache entry for key: {}, success: {}", cacheKey, deleted);
        } catch (Exception e) {
            handleRedisException(e, "delete", key);
        }
    }

    @Override
    public Set<String> keys(String pattern) {
        try {
            String searchPattern = generateCacheKey(pattern);
            Set<String> keys = redisTemplate.keys(searchPattern);
            logger.debug("Found {} keys matching pattern: {}", keys != null ? keys.size() : 0, searchPattern);
            return keys != null ? keys : Set.of();
        } catch (Exception e) {
            handleRedisException(e, "keys", pattern);
            return Set.of();
        }
    }

    @Override
    public void putAll(String key, Map<String, String> values) {
        try {
            String cacheKey = generateCacheKey(key);
            redisTemplate.opsForHash().putAll(cacheKey, values);
            logger.debug("Stored {} hash entries for key: {}", values.size(), cacheKey);
        } catch (Exception e) {
            handleRedisException(e, "putAll", key);
        }
    }

    /**
     * Handles Redis exceptions by logging and throwing infrastructure-specific exceptions.
     */
    private void handleRedisException(Exception e, String operation, String key) {
        logger.error("Redis operation '{}' failed for key '{}': {}", operation, key, e.getMessage(), e);
        throw new InfrastructureExceptionRedisException(
            "Redis operation failed: " + e.getMessage(), 
            operation, 
            key, 
            e
        );
    }

    /**
     * Generates a cache key with the appropriate prefix.
     */
    private String generateCacheKey(String key) {
        if (key.startsWith(CACHE_KEY_PREFIX)) {
            return key;
        }
        return CACHE_KEY_PREFIX + key;
    }
}