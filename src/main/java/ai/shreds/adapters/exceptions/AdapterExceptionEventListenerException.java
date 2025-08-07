package ai.shreds.adapters.exceptions;

/**
 * Exception thrown when errors occur during hardware event processing.
 * This exception is specific to the adapter layer and represents failures in
 * handling operating system hardware events such as device insertion or removal.
 */
public class AdapterExceptionEventListenerException extends RuntimeException {
    
    /**
     * Constructs a new event listener exception with the specified detail message.
     * 
     * @param message the detail message explaining the cause of the exception
     */
    public AdapterExceptionEventListenerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new event listener exception with the specified detail message and cause.
     * 
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of this exception
     */
    public AdapterExceptionEventListenerException(String message, Throwable cause) {
        super(message, cause);
    }
}