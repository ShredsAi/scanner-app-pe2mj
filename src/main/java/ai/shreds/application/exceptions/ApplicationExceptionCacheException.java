package ai.shreds.application.exceptions;

/**
 * Exception thrown when cache operations fail.
 * This exception represents errors that occur during cache management operations.
 */
public class ApplicationExceptionCacheException extends RuntimeException {

    private final String cacheKey;

    /**
     * Constructs a new cache exception with the specified detail message.
     * @param message the detail message
     */
    public ApplicationExceptionCacheException(String message) {
        super(message);
        this.cacheKey = null;
    }

    /**
     * Constructs a new cache exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ApplicationExceptionCacheException(String message, Throwable cause) {
        super(message, cause);
        this.cacheKey = null;
    }

    /**
     * Constructs a new cache exception with the specified detail message and cache key.
     * @param message the detail message
     * @param cacheKey the cache key that caused the error
     */
    public ApplicationExceptionCacheException(String message, String cacheKey) {
        super(message);
        this.cacheKey = cacheKey;
    }

    /**
     * Constructs a new cache exception with the specified detail message, cache key, and cause.
     * @param message the detail message
     * @param cacheKey the cache key that caused the error
     * @param cause the cause of the exception
     */
    public ApplicationExceptionCacheException(String message, String cacheKey, Throwable cause) {
        super(message, cause);
        this.cacheKey = cacheKey;
    }

    /**
     * Gets the cache key associated with this exception.
     * @return the cache key, or null if not specified
     */
    public String getCacheKey() {
        return cacheKey;
    }

    @Override
    public String toString() {
        return String.format("ApplicationExceptionCacheException{cacheKey='%s', message='%s'}", 
                cacheKey, getMessage());
    }
}