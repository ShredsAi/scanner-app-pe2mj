package ai.shreds.domain.exceptions;

import ai.shreds.shared.enums.SharedEnumDriverType;

/**
 * Domain exception thrown when driver communication errors occur.
 */
public class DomainExceptionDriverError extends RuntimeException {
    private final SharedEnumDriverType driverType;
    private final String errorCode;

    public DomainExceptionDriverError(String message, SharedEnumDriverType driverType, String errorCode) {
        super(message);
        this.driverType = driverType;
        this.errorCode = errorCode;
    }

    public DomainExceptionDriverError(String message, SharedEnumDriverType driverType, String errorCode, Throwable cause) {
        super(message, cause);
        this.driverType = driverType;
        this.errorCode = errorCode;
    }

    public SharedEnumDriverType getDriverType() {
        return driverType;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return "DomainExceptionDriverError{" +
               "driverType=" + driverType +
               ", errorCode='" + errorCode + '\'' +
               ", message='" + getMessage() + '\'' +
               '}';
    }
}