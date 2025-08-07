package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.domain.entities.DomainEntityDeviceConnection;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object representing a device connection.
 */
public class SharedDeviceConnectionDTO {
    
    @JsonProperty("connectionId")
    private String connectionId;
    
    @JsonProperty("scannerId")
    private SharedValueScannerId scannerId;
    
    @JsonProperty("protocol")
    private SharedEnumProtocolType protocol;
    
    @JsonProperty("establishedAt")
    private Instant establishedAt;
    
    @JsonProperty("lastHeartbeat")
    private Instant lastHeartbeat;
    
    @JsonProperty("isConnected")
    private Boolean isConnected;
    
    @JsonProperty("connectionStatus")
    private String connectionStatus;
    
    @JsonProperty("connectionParams")
    private Map<String, String> connectionParams;
    
    // Default constructor for JSON deserialization
    public SharedDeviceConnectionDTO() {}
    
    // Constructor with all fields
    public SharedDeviceConnectionDTO(String connectionId,
                                   SharedValueScannerId scannerId,
                                   SharedEnumProtocolType protocol,
                                   Instant establishedAt,
                                   Instant lastHeartbeat,
                                   Boolean isConnected,
                                   String connectionStatus,
                                   Map<String, String> connectionParams) {
        this.connectionId = connectionId;
        this.scannerId = scannerId;
        this.protocol = protocol;
        this.establishedAt = establishedAt;
        this.lastHeartbeat = lastHeartbeat;
        this.isConnected = isConnected;
        this.connectionStatus = connectionStatus;
        this.connectionParams = connectionParams;
    }
    
    /**
     * Converts this DTO to a domain entity.
     * @return DomainEntityDeviceConnection representing this DTO
     */
    public DomainEntityDeviceConnection toEntity() {
        return new DomainEntityDeviceConnection(
            connectionId != null ? connectionId : UUID.randomUUID().toString(),
            scannerId,
            protocol,
            establishedAt,
            lastHeartbeat,
            connectionParams,
            null // driver will be set separately if needed
        );
    }
    
    /**
     * Creates a DTO from a domain entity.
     * @param connection the domain entity to convert
     * @return SharedDeviceConnectionDTO representing the domain entity
     */
    public static SharedDeviceConnectionDTO fromEntity(DomainEntityDeviceConnection connection) {
        if (connection == null) {
            return null;
        }
        
        return new SharedDeviceConnectionDTO(
            connection.getConnectionId(),
            connection.getScannerId(),
            connection.getProtocol(),
            connection.getEstablishedAt(),
            connection.getLastHeartbeat(),
            connection.isConnected(),
            connection.isConnected() ? "CONNECTED" : "DISCONNECTED",
            connection.getConnectionParams()
        );
    }
    
    // Getters and Setters
    public String getConnectionId() { return connectionId; }
    public void setConnectionId(String connectionId) { this.connectionId = connectionId; }
    
    public SharedValueScannerId getScannerId() { return scannerId; }
    public void setScannerId(SharedValueScannerId scannerId) { this.scannerId = scannerId; }
    
    public SharedEnumProtocolType getProtocol() { return protocol; }
    public void setProtocol(SharedEnumProtocolType protocol) { this.protocol = protocol; }
    
    public Instant getEstablishedAt() { return establishedAt; }
    public void setEstablishedAt(Instant establishedAt) { this.establishedAt = establishedAt; }
    
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public void setLastHeartbeat(Instant lastHeartbeat) { this.lastHeartbeat = lastHeartbeat; }
    
    public Boolean getIsConnected() { return isConnected; }
    public void setIsConnected(Boolean isConnected) { this.isConnected = isConnected; }
    
    public String getConnectionStatus() { return connectionStatus; }
    public void setConnectionStatus(String connectionStatus) { this.connectionStatus = connectionStatus; }
    
    public Map<String, String> getConnectionParams() { return connectionParams; }
    public void setConnectionParams(Map<String, String> connectionParams) { this.connectionParams = connectionParams; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDeviceConnectionDTO)) return false;
        SharedDeviceConnectionDTO that = (SharedDeviceConnectionDTO) o;
        return Objects.equals(connectionId, that.connectionId) &&
               Objects.equals(scannerId, that.scannerId) &&
               protocol == that.protocol &&
               Objects.equals(establishedAt, that.establishedAt) &&
               Objects.equals(lastHeartbeat, that.lastHeartbeat) &&
               Objects.equals(isConnected, that.isConnected) &&
               Objects.equals(connectionStatus, that.connectionStatus) &&
               Objects.equals(connectionParams, that.connectionParams);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(connectionId, scannerId, protocol, establishedAt,
                          lastHeartbeat, isConnected, connectionStatus, connectionParams);
    }
    
    @Override
    public String toString() {
        return "SharedDeviceConnectionDTO{" +
               "connectionId='" + connectionId + '\'' +
               ", scannerId=" + scannerId +
               ", protocol=" + protocol +
               ", establishedAt=" + establishedAt +
               ", lastHeartbeat=" + lastHeartbeat +
               ", isConnected=" + isConnected +
               ", connectionStatus='" + connectionStatus + '\'' +
               ", connectionParams=" + connectionParams +
               '}';
    }
}