package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when native library function calls fail.
 * Provides specific information about the error code and native function that failed.
 */
public class InfrastructureExceptionNativeLibraryException extends RuntimeException {

    private final int errorCode;
    private final String nativeFunction;

    /**
     * Constructs a new native library exception with the specified message, error code, and function name.
     *
     * @param message the detail message
     * @param errorCode the native error code returned by the function
     * @param nativeFunction the name of the native function that failed
     */
    public InfrastructureExceptionNativeLibraryException(String message, int errorCode, String nativeFunction) {
        super(message);
        this.errorCode = errorCode;
        this.nativeFunction = nativeFunction;
    }

    /**
     * Constructs a new native library exception with the specified message, error code, function name, and cause.
     *
     * @param message the detail message
     * @param errorCode the native error code returned by the function
     * @param nativeFunction the name of the native function that failed
     * @param cause the cause of the exception
     */
    public InfrastructureExceptionNativeLibraryException(String message, int errorCode, String nativeFunction, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.nativeFunction = nativeFunction;
    }

    /**
     * Gets the native error code returned by the failed function.
     *
     * @return the error code
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Gets the name of the native function that failed.
     *
     * @return the native function name
     */
    public String getNativeFunction() {
        return nativeFunction;
    }

    @Override
    public String toString() {
        return String.format("%s: %s (Error Code: %d, Function: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            errorCode, 
            nativeFunction != null ? nativeFunction : "unknown");
    }
}