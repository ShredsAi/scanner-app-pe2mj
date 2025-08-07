package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Event representing a scanner device connection.
 */
public class SharedScannerConnectedEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("scannerId")
    private String scannerId;
    
    @JsonProperty("connectionDetails")
    private SharedDeviceConnectionDTO connectionDetails;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for JSON deserialization
    public SharedScannerConnectedEvent() {
        this.eventType = "ScannerConnectedEvent";
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }
    
    /**
     * Constructor with scanner ID and connection details.
     * @param scannerId the scanner identifier
     * @param connectionDetails the connection details
     */
    public SharedScannerConnectedEvent(String scannerId, SharedDeviceConnectionDTO connectionDetails) {
        this();
        this.scannerId = scannerId;
        this.connectionDetails = connectionDetails;
    }
    
    /**
     * Constructor with all fields.
     */
    public SharedScannerConnectedEvent(String eventType, Instant timestamp, String scannerId,
                                      SharedDeviceConnectionDTO connectionDetails, String correlationId) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.scannerId = scannerId;
        this.connectionDetails = connectionDetails;
        this.correlationId = correlationId;
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public String getScannerId() { return scannerId; }
    public void setScannerId(String scannerId) { this.scannerId = scannerId; }
    
    public SharedDeviceConnectionDTO getConnectionDetails() { return connectionDetails; }
    public void setConnectionDetails(SharedDeviceConnectionDTO connectionDetails) { this.connectionDetails = connectionDetails; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerConnectedEvent)) return false;
        SharedScannerConnectedEvent that = (SharedScannerConnectedEvent) o;
        return Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(scannerId, that.scannerId) &&
               Objects.equals(connectionDetails, that.connectionDetails) &&
               Objects.equals(correlationId, that.correlationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventType, timestamp, scannerId, connectionDetails, correlationId);
    }
    
    @Override
    public String toString() {
        return "SharedScannerConnectedEvent{" +
               "eventType='" + eventType + '\'' +
               ", timestamp=" + timestamp +
               ", scannerId='" + scannerId + '\'' +
               ", connectionDetails=" + connectionDetails +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}