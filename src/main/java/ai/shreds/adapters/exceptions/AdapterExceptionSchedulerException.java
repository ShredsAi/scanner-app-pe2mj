package ai.shreds.adapters.exceptions;

/**
 * Exception thrown when errors occur during scheduled scanner discovery operations.
 * This exception is specific to the adapter layer and represents failures in
 * the scheduling and execution of discovery tasks.
 */
public class AdapterExceptionSchedulerException extends RuntimeException {
    
    /**
     * Constructs a new scheduler exception with the specified detail message.
     * 
     * @param message the detail message explaining the cause of the exception
     */
    public AdapterExceptionSchedulerException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new scheduler exception with the specified detail message and cause.
     * 
     * @param message the detail message explaining the cause of the exception
     * @param cause the underlying cause of this exception
     */
    public AdapterExceptionSchedulerException(String message, Throwable cause) {
        super(message, cause);
    }
}