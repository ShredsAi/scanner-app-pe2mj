package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Event representing a scanner device disconnection.
 */
public class SharedScannerDisconnectedEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("scannerId")
    private String scannerId;
    
    @JsonProperty("reason")
    private String reason;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for JSON deserialization
    public SharedScannerDisconnectedEvent() {
        this.eventType = "ScannerDisconnectedEvent";
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }
    
    /**
     * Constructor with scanner ID and reason.
     * @param scannerId the scanner identifier
     * @param reason the disconnection reason
     */
    public SharedScannerDisconnectedEvent(String scannerId, String reason) {
        this();
        this.scannerId = scannerId;
        this.reason = reason;
    }
    
    /**
     * Constructor with all fields.
     */
    public SharedScannerDisconnectedEvent(String eventType, Instant timestamp, 
                                         String scannerId, String reason, String correlationId) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.scannerId = scannerId;
        this.reason = reason;
        this.correlationId = correlationId;
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getScannerId() { return scannerId; }
    public void setScannerId(String scannerId) { this.scannerId = scannerId; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerDisconnectedEvent)) return false;
        SharedScannerDisconnectedEvent that = (SharedScannerDisconnectedEvent) o;
        return Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(scannerId, that.scannerId) &&
               Objects.equals(reason, that.reason) &&
               Objects.equals(correlationId, that.correlationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventType, timestamp, scannerId, reason, correlationId);
    }
    
    @Override
    public String toString() {
        return "SharedScannerDisconnectedEvent{" +
               "eventType='" + eventType + '\'' +
               ", timestamp=" + timestamp +
               ", scannerId='" + scannerId + '\'' +
               ", reason='" + reason + '\'' +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}