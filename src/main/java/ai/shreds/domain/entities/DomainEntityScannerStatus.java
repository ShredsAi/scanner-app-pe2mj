package ai.shreds.domain.entities;

import ai.shreds.shared.enums.SharedEnumConnectionState;
import ai.shreds.shared.enums.SharedEnumAvailabilityState;
import ai.shreds.shared.dtos.SharedScannerStatusDTO;
import ai.shreds.domain.value_objects.DomainValueErrorState;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing scanner status information.
 */
public class DomainEntityScannerStatus {
    private SharedEnumConnectionState connectionState;
    private SharedEnumAvailabilityState availability;
    private Instant lastSeen;
    private DomainValueErrorState errorState;
    private DomainEntityDeviceConnection connection;

    public DomainEntityScannerStatus(SharedEnumConnectionState connectionState, 
                                    SharedEnumAvailabilityState availability, 
                                    Instant lastSeen, 
                                    DomainValueErrorState errorState, 
                                    DomainEntityDeviceConnection connection) {
        this.connectionState = Objects.requireNonNull(connectionState, "Connection state cannot be null");
        this.availability = Objects.requireNonNull(availability, "Availability cannot be null");
        this.lastSeen = lastSeen != null ? lastSeen : Instant.now();
        this.errorState = errorState;
        this.connection = connection;
    }

    public void updateConnectionState(SharedEnumConnectionState state) {
        this.connectionState = Objects.requireNonNull(state, "Connection state cannot be null");
        this.lastSeen = Instant.now();
        
        // Clear error state if connection is restored
        if (state == SharedEnumConnectionState.CONNECTED && this.errorState != null) {
            this.errorState = null;
        }
    }

    public void markAsBusy() {
        this.availability = SharedEnumAvailabilityState.BUSY;
        this.lastSeen = Instant.now();
    }

    public void markAsAvailable() {
        this.availability = SharedEnumAvailabilityState.AVAILABLE;
        this.lastSeen = Instant.now();
    }

    public void recordHeartbeat() {
        this.lastSeen = Instant.now();
        
        // If we receive a heartbeat, the device is connected
        if (this.connectionState != SharedEnumConnectionState.CONNECTED) {
            updateConnectionState(SharedEnumConnectionState.CONNECTED);
        }
    }

    public boolean isHealthy() {
        return connectionState == SharedEnumConnectionState.CONNECTED && 
               availability != SharedEnumAvailabilityState.OFFLINE &&
               errorState == null;
    }

    public Duration getTimeSinceLastSeen() {
        return Duration.between(lastSeen, Instant.now());
    }

    public void setErrorState(DomainValueErrorState errorState) {
        this.errorState = errorState;
        this.connectionState = SharedEnumConnectionState.ERROR;
        this.availability = SharedEnumAvailabilityState.OFFLINE;
        this.lastSeen = Instant.now();
    }

    public SharedScannerStatusDTO toDTO() {
        return SharedScannerStatusDTO.fromEntity(this);
    }

    public static DomainEntityScannerStatus fromDTO(SharedScannerStatusDTO dto) {
        if (dto == null) {
            return null;
        }
        return dto.toEntity();
    }

    // Getters
    public SharedEnumConnectionState getConnectionState() { return connectionState; }
    public SharedEnumAvailabilityState getAvailability() { return availability; }
    public Instant getLastSeen() { return lastSeen; }
    public DomainValueErrorState getErrorState() { return errorState; }
    public DomainEntityDeviceConnection getConnection() { return connection; }

    public void setConnection(DomainEntityDeviceConnection connection) {
        this.connection = connection;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainEntityScannerStatus that = (DomainEntityScannerStatus) other;
        return connectionState == that.connectionState &&
               availability == that.availability &&
               Objects.equals(lastSeen, that.lastSeen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(connectionState, availability, lastSeen);
    }

    @Override
    public String toString() {
        return "DomainEntityScannerStatus{" +
               "connectionState=" + connectionState +
               ", availability=" + availability +
               ", lastSeen=" + lastSeen +
               ", hasError=" + (errorState != null) +
               '}';
    }
}