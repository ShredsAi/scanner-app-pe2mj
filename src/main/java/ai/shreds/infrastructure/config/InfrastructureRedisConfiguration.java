package ai.shreds.infrastructure.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis configuration for the scanner hardware discovery service.
 * Configures Redis connection, connection pooling, and serialization.
 */
@Configuration
public class InfrastructureRedisConfiguration {

    @Value("${redis.host:localhost}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private Integer redisPort;

    @Value("${redis.password:}")
    private String redisPassword;

    @Value("${redis.pool.max-active:8}")
    private Integer connectionPoolSize;

    @Value("${redis.timeout:2000}")
    private Integer connectionTimeout;

    @Value("${redis.pool.max-idle:8}")
    private Integer maxIdleConnections;

    @Value("${redis.pool.min-idle:0}")
    private Integer minIdleConnections;

    @Value("${redis.pool.max-wait:-1}")
    private Long maxWaitMillis;

    /**
     * Configures the Redis connection factory with connection pooling.
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // Configure Redis standalone connection
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);
        
        if (redisPassword != null && !redisPassword.trim().isEmpty()) {
            redisConfig.setPassword(redisPassword);
        }

        // Configure connection pool
        GenericObjectPoolConfig<?> poolConfig = configureConnectionPool();
        
        // Configure Lettuce client with pooling
        LettucePoolingClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .poolConfig(poolConfig)
                .commandTimeout(Duration.ofMillis(connectionTimeout))
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    /**
     * Configures the Redis template with appropriate serializers.
     */
    @Bean
    public RedisTemplate<String, String> redisTemplate() {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        
        // Configure serializers
        RedisSerializer<String> stringSerializer = configureSerializer();
        
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        // Enable transaction support
        template.setEnableTransactionSupport(true);
        
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configures the Redis cache manager.
     */
    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.Builder builder = RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory())
                .cacheDefaults(
                    org.springframework.data.redis.cache.RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(5)) // Default TTL for cache entries
                        .serializeKeysWith(
                            org.springframework.data.redis.cache.RedisCacheConfiguration.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                        )
                        .serializeValuesWith(
                            org.springframework.data.redis.cache.RedisCacheConfiguration.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                        )
                );

        return builder.build();
    }

    /**
     * Configures the Redis connection pool settings.
     */
    private GenericObjectPoolConfig<?> configureConnectionPool() {
        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        
        poolConfig.setMaxTotal(connectionPoolSize);
        poolConfig.setMaxIdle(maxIdleConnections);
        poolConfig.setMinIdle(minIdleConnections);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        
        // Additional pool configuration
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofMinutes(1).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(3);
        poolConfig.setBlockWhenExhausted(true);
        
        return poolConfig;
    }

    /**
     * Configures the Redis serializer.
     */
    private RedisSerializer<String> configureSerializer() {
        return new StringRedisSerializer();
    }

    /**
     * Gets the Redis host configuration.
     */
    public String getRedisHost() {
        return redisHost;
    }

    /**
     * Gets the Redis port configuration.
     */
    public Integer getRedisPort() {
        return redisPort;
    }

    /**
     * Gets the connection pool size configuration.
     */
    public Integer getConnectionPoolSize() {
        return connectionPoolSize;
    }

    /**
     * Gets the connection timeout configuration.
     */
    public Integer getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Provides Redis connection information for monitoring.
     */
    @Bean
    public RedisConnectionInfo redisConnectionInfo() {
        return new RedisConnectionInfo(redisHost, redisPort, connectionPoolSize, connectionTimeout);
    }

    /**
     * Redis connection information class for monitoring and diagnostics.
     */
    public static class RedisConnectionInfo {
        private final String host;
        private final Integer port;
        private final Integer poolSize;
        private final Integer timeout;

        public RedisConnectionInfo(String host, Integer port, Integer poolSize, Integer timeout) {
            this.host = host;
            this.port = port;
            this.poolSize = poolSize;
            this.timeout = timeout;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        public Integer getPoolSize() {
            return poolSize;
        }

        public Integer getTimeout() {
            return timeout;
        }

        public String getConnectionString() {
            return String.format("redis://%s:%d", host, port);
        }

        @Override
        public String toString() {
            return String.format("RedisConnectionInfo{host='%s', port=%d, poolSize=%d, timeout=%d}",
                               host, port, poolSize, timeout);
        }
    }
}