package ai.shreds.domain.entities;

import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.shared.dtos.SharedScannerDTO;
import java.time.Instant;
import java.util.Objects;

/**
 * Domain entity representing a scanner device.
 */
public class DomainEntityScanner {
    private final SharedValueScannerId scannerId;
    private final String deviceName;
    private final String manufacturer;
    private final String model;
    private final String serialNumber;
    private boolean isActive;
    private DomainEntityScannerCapabilities capabilities;
    private DomainEntityScannerStatus status;
    private final Instant createdAt;
    private Instant updatedAt;

    public DomainEntityScanner(SharedValueScannerId scannerId, String deviceName, String manufacturer, 
                              String model, String serialNumber, boolean isActive, 
                              DomainEntityScannerCapabilities capabilities, DomainEntityScannerStatus status, 
                              Instant createdAt, Instant updatedAt) {
        this.scannerId = Objects.requireNonNull(scannerId, "Scanner ID cannot be null");
        this.deviceName = Objects.requireNonNull(deviceName, "Device name cannot be null");
        this.manufacturer = Objects.requireNonNull(manufacturer, "Manufacturer cannot be null");
        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.serialNumber = serialNumber; // Can be null for some devices
        this.isActive = isActive;
        this.capabilities = capabilities;
        this.status = status;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }

    public void updateCapabilities(DomainEntityScannerCapabilities capabilities) {
        this.capabilities = Objects.requireNonNull(capabilities, "Capabilities cannot be null");
        this.updatedAt = Instant.now();
    }

    public boolean isAvailable() {
        return isActive && status != null && status.isHealthy();
    }

    public String getFullDeviceName() {
        return manufacturer + " " + model + (deviceName.equals(model) ? "" : " (" + deviceName + ")");
    }

    public SharedScannerDTO toDTO() {
        return SharedScannerDTO.fromEntity(this);
    }

    public static DomainEntityScanner fromDTO(SharedScannerDTO dto) {
        if (dto == null) {
            return null;
        }
        return dto.toEntity();
    }

    // Getters
    public SharedValueScannerId getScannerId() { return scannerId; }
    public String getDeviceName() { return deviceName; }
    public String getManufacturer() { return manufacturer; }
    public String getModel() { return model; }
    public String getSerialNumber() { return serialNumber; }
    public boolean isActive() { return isActive; }
    public DomainEntityScannerCapabilities getCapabilities() { return capabilities; }
    public DomainEntityScannerStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setStatus(DomainEntityScannerStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainEntityScanner scanner = (DomainEntityScanner) other;
        return Objects.equals(scannerId, scanner.scannerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scannerId);
    }

    @Override
    public String toString() {
        return "DomainEntityScanner{" +
               "scannerId=" + scannerId +
               ", deviceName='" + deviceName + '\'' +
               ", manufacturer='" + manufacturer + '\'' +
               ", model='" + model + '\'' +
               ", isActive=" + isActive +
               '}';
    }
}