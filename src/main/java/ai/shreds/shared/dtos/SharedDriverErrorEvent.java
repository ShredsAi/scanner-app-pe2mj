package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumDriverType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Event representing a driver error.
 */
public class SharedDriverErrorEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("scannerId")
    private String scannerId;
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("driverType")
    private SharedEnumDriverType driverType;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for JSON deserialization
    public SharedDriverErrorEvent() {
        this.eventType = "DriverErrorEvent";
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }
    
    /**
     * Constructor with error details.
     * @param scannerId the scanner identifier
     * @param errorCode the error code
     * @param errorMessage the error message
     * @param driverType the driver type
     */
    public SharedDriverErrorEvent(String scannerId, String errorCode, 
                                 String errorMessage, SharedEnumDriverType driverType) {
        this();
        this.scannerId = scannerId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.driverType = driverType;
    }
    
    /**
     * Constructor with all fields.
     */
    public SharedDriverErrorEvent(String eventType, Instant timestamp, String scannerId,
                                 String errorCode, String errorMessage, 
                                 SharedEnumDriverType driverType, String correlationId) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.scannerId = scannerId;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.driverType = driverType;
        this.correlationId = correlationId;
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getScannerId() { return scannerId; }
    public void setScannerId(String scannerId) { this.scannerId = scannerId; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public SharedEnumDriverType getDriverType() { return driverType; }
    public void setDriverType(SharedEnumDriverType driverType) { this.driverType = driverType; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDriverErrorEvent)) return false;
        SharedDriverErrorEvent that = (SharedDriverErrorEvent) o;
        return Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(scannerId, that.scannerId) &&
               Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               driverType == that.driverType &&
               Objects.equals(correlationId, that.correlationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventType, timestamp, scannerId, errorCode, 
                          errorMessage, driverType, correlationId);
    }
    
    @Override
    public String toString() {
        return "SharedDriverErrorEvent{" +
               "eventType='" + eventType + '\'' +
               ", timestamp=" + timestamp +
               ", scannerId='" + scannerId + '\'' +
               ", errorCode='" + errorCode + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               ", driverType=" + driverType +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}