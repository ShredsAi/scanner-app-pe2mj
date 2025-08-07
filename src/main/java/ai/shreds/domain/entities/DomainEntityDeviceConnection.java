package ai.shreds.domain.entities;

import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.shared.dtos.SharedDeviceConnectionDTO;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Domain entity representing a device connection.
 */
public class DomainEntityDeviceConnection {
    private final String connectionId;
    private final SharedValueScannerId scannerId;
    private final SharedEnumProtocolType protocol;
    private final Instant establishedAt;
    private Instant lastHeartbeat;
    private final Map<String, String> connectionParams;
    private DomainEntityScannerDriver driver;

    public DomainEntityDeviceConnection(String connectionId, SharedValueScannerId scannerId, 
                                       SharedEnumProtocolType protocol, Instant establishedAt, 
                                       Instant lastHeartbeat, Map<String, String> connectionParams, 
                                       DomainEntityScannerDriver driver) {
        this.connectionId = Objects.requireNonNull(connectionId, "Connection ID cannot be null");
        this.scannerId = Objects.requireNonNull(scannerId, "Scanner ID cannot be null");
        this.protocol = Objects.requireNonNull(protocol, "Protocol cannot be null");
        this.establishedAt = establishedAt != null ? establishedAt : Instant.now();
        this.lastHeartbeat = lastHeartbeat != null ? lastHeartbeat : Instant.now();
        this.connectionParams = connectionParams != null ? new HashMap<>(connectionParams) : new HashMap<>();
        this.driver = driver;
    }

    public void sendHeartbeat() {
        this.lastHeartbeat = Instant.now();
    }

    public boolean isConnectionAlive(Duration timeout) {
        if (timeout == null) {
            timeout = Duration.ofMinutes(2); // Default timeout
        }
        Duration timeSinceLastHeartbeat = Duration.between(lastHeartbeat, Instant.now());
        return timeSinceLastHeartbeat.compareTo(timeout) <= 0;
    }

    public Duration getConnectionDuration() {
        return Duration.between(establishedAt, Instant.now());
    }

    public void updateConnectionParams(Map<String, String> params) {
        if (params != null) {
            this.connectionParams.clear();
            this.connectionParams.putAll(params);
        }
    }

    public void addConnectionParam(String key, String value) {
        if (key != null && value != null) {
            this.connectionParams.put(key, value);
        }
    }

    public String getConnectionParam(String key) {
        return this.connectionParams.get(key);
    }

    public boolean isConnected() {
        return isConnectionAlive(Duration.ofMinutes(2));
    }

    public SharedDeviceConnectionDTO toDTO() {
        return SharedDeviceConnectionDTO.fromEntity(this);
    }

    public static DomainEntityDeviceConnection fromDTO(SharedDeviceConnectionDTO dto) {
        if (dto == null) {
            return null;
        }
        return dto.toEntity();
    }

    // Getters
    public String getConnectionId() { return connectionId; }
    public SharedValueScannerId getScannerId() { return scannerId; }
    public SharedEnumProtocolType getProtocol() { return protocol; }
    public Instant getEstablishedAt() { return establishedAt; }
    public Instant getLastHeartbeat() { return lastHeartbeat; }
    public Map<String, String> getConnectionParams() { return new HashMap<>(connectionParams); }
    public DomainEntityScannerDriver getDriver() { return driver; }

    public void setDriver(DomainEntityScannerDriver driver) {
        this.driver = driver;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainEntityDeviceConnection that = (DomainEntityDeviceConnection) other;
        return Objects.equals(connectionId, that.connectionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionId);
    }

    @Override
    public String toString() {
        return "DomainEntityDeviceConnection{" +
               "connectionId='" + connectionId + '\'' +
               ", scannerId=" + scannerId +
               ", protocol=" + protocol +
               ", establishedAt=" + establishedAt +
               ", isAlive=" + isConnected() +
               '}';
    }
}