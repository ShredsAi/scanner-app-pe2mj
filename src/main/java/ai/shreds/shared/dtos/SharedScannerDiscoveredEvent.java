package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Event representing the discovery of a new scanner device.
 */
public class SharedScannerDiscoveredEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("scanner")
    private SharedScannerDTO scanner;
    
    @JsonProperty("correlationId")
    private String correlationId;
    
    // Default constructor for JSON deserialization
    public SharedScannerDiscoveredEvent() {
        this.eventType = "ScannerDiscoveredEvent";
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID().toString();
    }
    
    /**
     * Constructor with scanner information.
     * @param scanner the discovered scanner
     */
    public SharedScannerDiscoveredEvent(SharedScannerDTO scanner) {
        this();
        this.scanner = scanner;
    }
    
    /**
     * Constructor with all fields.
     */
    public SharedScannerDiscoveredEvent(String eventType, Instant timestamp, 
                                       SharedScannerDTO scanner, String correlationId) {
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.scanner = scanner;
        this.correlationId = correlationId;
    }
    
    // Getters and Setters
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    
    public SharedScannerDTO getScanner() { return scanner; }
    public void setScanner(SharedScannerDTO scanner) { this.scanner = scanner; }
    
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerDiscoveredEvent)) return false;
        SharedScannerDiscoveredEvent that = (SharedScannerDiscoveredEvent) o;
        return Objects.equals(eventType, that.eventType) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(scanner, that.scanner) &&
               Objects.equals(correlationId, that.correlationId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventType, timestamp, scanner, correlationId);
    }
    
    @Override
    public String toString() {
        return "SharedScannerDiscoveredEvent{" +
               "eventType='" + eventType + '\'' +
               ", timestamp=" + timestamp +
               ", scanner=" + scanner +
               ", correlationId='" + correlationId + '\'' +
               '}';
    }
}