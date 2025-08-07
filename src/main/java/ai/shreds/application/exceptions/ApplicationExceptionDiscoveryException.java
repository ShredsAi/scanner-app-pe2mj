package ai.shreds.application.exceptions;

/**
 * Exception thrown when scanner discovery operations fail.
 * This exception represents errors that occur during the scanner discovery process.
 */
public class ApplicationExceptionDiscoveryException extends RuntimeException {

    private final String errorCode;

    /**
     * Constructs a new discovery exception with the specified detail message.
     * @param message the detail message
     */
    public ApplicationExceptionDiscoveryException(String message) {
        super(message);
        this.errorCode = "DISCOVERY_ERROR";
    }

    /**
     * Constructs a new discovery exception with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ApplicationExceptionDiscoveryException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "DISCOVERY_ERROR";
    }

    /**
     * Constructs a new discovery exception with the specified detail message and error code.
     * @param message the detail message
     * @param errorCode the specific error code
     */
    public ApplicationExceptionDiscoveryException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode != null ? errorCode : "DISCOVERY_ERROR";
    }

    /**
     * Constructs a new discovery exception with the specified detail message, error code, and cause.
     * @param message the detail message
     * @param errorCode the specific error code
     * @param cause the cause of the exception
     */
    public ApplicationExceptionDiscoveryException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode != null ? errorCode : "DISCOVERY_ERROR";
    }

    /**
     * Gets the error code associated with this exception.
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("ApplicationExceptionDiscoveryException{errorCode='%s', message='%s'}", 
                errorCode, getMessage());
    }
}