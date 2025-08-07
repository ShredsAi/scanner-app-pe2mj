package ai.shreds.domain.ports;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Port for cache repository operations in the domain layer.
 */
public interface DomainOutputPortCacheRepository {

    /**
     * Save a value in cache with a TTL.
     * @param key cache key
     * @param value cached value
     * @param ttl time to live
     */
    void save(String key, String value, Duration ttl);

    /**
     * Retrieve a value from cache by key.
     * @param key cache key
     * @return Optional containing the cached value or empty if not found
     */
    Optional<String> get(String key);

    /**
     * Delete a cache entry by key.
     * @param key cache key
     */
    void delete(String key);

    /**
     * Retrieve all keys matching a pattern.
     * @param pattern key pattern
     * @return set of matching keys
     */
    Set<String> keys(String pattern);

    /**
     * Store multiple values in a hash under a single key.
     * @param key cache key
     * @param values map of field-value pairs
     */
    void putAll(String key, Map<String, String> values);
}
