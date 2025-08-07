package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when Redis operations fail.
 * Provides specific information about the failed operation and key.
 */
public class InfrastructureExceptionRedisException extends RuntimeException {

    private final String operation;
    private final String key;

    /**
     * Constructs a new Redis exception with the specified message, operation, and key.
     *
     * @param message the detail message
     * @param operation the Redis operation that failed
     * @param key the cache key involved in the operation
     */
    public InfrastructureExceptionRedisException(String message, String operation, String key) {
        super(message);
        this.operation = operation;
        this.key = key;
    }

    /**
     * Constructs a new Redis exception with the specified message, operation, key, and cause.
     *
     * @param message the detail message
     * @param operation the Redis operation that failed
     * @param key the cache key involved in the operation
     * @param cause the cause of the exception
     */
    public InfrastructureExceptionRedisException(String message, String operation, String key, Throwable cause) {
        super(message, cause);
        this.operation = operation;
        this.key = key;
    }

    /**
     * Gets the Redis operation that failed.
     *
     * @return the operation name
     */
    public String getOperation() {
        return operation;
    }

    /**
     * Gets the cache key involved in the failed operation.
     *
     * @return the cache key
     */
    public String getKey() {
        return key;
    }
}