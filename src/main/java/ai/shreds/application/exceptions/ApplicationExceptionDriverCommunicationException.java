package ai.shreds.application.exceptions;

import ai.shreds.shared.enums.SharedEnumDriverType;

/**
 * Exception thrown when driver communication operations fail.
 * This exception represents errors that occur during communication with scanner drivers.
 */
public class ApplicationExceptionDriverCommunicationException extends RuntimeException {

    private final SharedEnumDriverType driverType;
    private final String errorCode;

    /**
     * Constructs a new driver communication exception with the specified detail message and driver type.
     * @param message the detail message
     * @param driverType the type of driver that caused the error
     */
    public ApplicationExceptionDriverCommunicationException(String message, SharedEnumDriverType driverType) {
        super(message);
        this.driverType = driverType;
        this.errorCode = "DRIVER_COMMUNICATION_ERROR";
    }

    /**
     * Constructs a new driver communication exception with the specified detail message, driver type, and cause.
     * @param message the detail message
     * @param driverType the type of driver that caused the error
     * @param cause the cause of the exception
     */
    public ApplicationExceptionDriverCommunicationException(String message, SharedEnumDriverType driverType, Throwable cause) {
        super(message, cause);
        this.driverType = driverType;
        this.errorCode = "DRIVER_COMMUNICATION_ERROR";
    }

    /**
     * Constructs a new driver communication exception with the specified detail message, driver type, and error code.
     * @param message the detail message
     * @param driverType the type of driver that caused the error
     * @param errorCode the specific error code
     */
    public ApplicationExceptionDriverCommunicationException(String message, SharedEnumDriverType driverType, String errorCode) {
        super(message);
        this.driverType = driverType;
        this.errorCode = errorCode != null ? errorCode : "DRIVER_COMMUNICATION_ERROR";
    }

    /**
     * Constructs a new driver communication exception with the specified detail message, driver type, error code, and cause.
     * @param message the detail message
     * @param driverType the type of driver that caused the error
     * @param errorCode the specific error code
     * @param cause the cause of the exception
     */
    public ApplicationExceptionDriverCommunicationException(String message, SharedEnumDriverType driverType, String errorCode, Throwable cause) {
        super(message, cause);
        this.driverType = driverType;
        this.errorCode = errorCode != null ? errorCode : "DRIVER_COMMUNICATION_ERROR";
    }

    /**
     * Gets the driver type associated with this exception.
     * @return the driver type
     */
    public SharedEnumDriverType getDriverType() {
        return driverType;
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
        return String.format("ApplicationExceptionDriverCommunicationException{driverType=%s, errorCode='%s', message='%s'}", 
                driverType, errorCode, getMessage());
    }
}