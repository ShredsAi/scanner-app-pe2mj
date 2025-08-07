package ai.shreds.domain.exceptions;

/**
 * Domain exception thrown when scanner capabilities are invalid or inconsistent.
 */
public class DomainExceptionInvalidCapabilities extends RuntimeException {
    private final String capabilityType;
    private final Object invalidValue;

    public DomainExceptionInvalidCapabilities(String message, String capabilityType, Object invalidValue) {
        super(message);
        this.capabilityType = capabilityType;
        this.invalidValue = invalidValue;
    }

    public DomainExceptionInvalidCapabilities(String message, String capabilityType, Object invalidValue, Throwable cause) {
        super(message, cause);
        this.capabilityType = capabilityType;
        this.invalidValue = invalidValue;
    }

    public String getCapabilityType() {
        return capabilityType;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    @Override
    public String toString() {
        return "DomainExceptionInvalidCapabilities{" +
               "capabilityType='" + capabilityType + '\'' +
               ", invalidValue=" + invalidValue +
               ", message='" + getMessage() + '\'' +
               '}';
    }
}