package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.entities.DomainEntityScannerCapabilities;
import ai.shreds.domain.entities.DomainEntityScannerStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object representing a scanner device with its complete information.
 */
public class SharedScannerDTO {
    
    @JsonProperty("scannerId")
    private SharedValueScannerId scannerId;
    
    @JsonProperty("deviceName")
    private String deviceName;
    
    @JsonProperty("manufacturer")
    private String manufacturer;
    
    @JsonProperty("model")
    private String model;
    
    @JsonProperty("serialNumber")
    private String serialNumber;
    
    @JsonProperty("isActive")
    private Boolean isActive;
    
    @JsonProperty("capabilities")
    private SharedScannerCapabilitiesDTO capabilities;
    
    @JsonProperty("status")
    private SharedScannerStatusDTO status;
    
    @JsonProperty("createdAt")
    private Instant createdAt;
    
    @JsonProperty("updatedAt")
    private Instant updatedAt;
    
    // Default constructor for JSON deserialization
    public SharedScannerDTO() {}
    
    // Constructor with all fields
    public SharedScannerDTO(SharedValueScannerId scannerId,
                           String deviceName,
                           String manufacturer,
                           String model,
                           String serialNumber,
                           Boolean isActive,
                           SharedScannerCapabilitiesDTO capabilities,
                           SharedScannerStatusDTO status,
                           Instant createdAt,
                           Instant updatedAt) {
        this.scannerId = scannerId;
        this.deviceName = deviceName;
        this.manufacturer = manufacturer;
        this.model = model;
        this.serialNumber = serialNumber;
        this.isActive = isActive;
        this.capabilities = capabilities;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    /**
     * Converts this DTO to a domain entity.
     * @return DomainEntityScanner representing this DTO
     */
    public DomainEntityScanner toEntity() {
        DomainEntityScannerCapabilities domainCapabilities = null;
        if (capabilities != null) {
            domainCapabilities = capabilities.toEntity();
        }
        
        DomainEntityScannerStatus domainStatus = null;
        if (status != null) {
            domainStatus = status.toEntity();
        }
        
        return new DomainEntityScanner(
            scannerId,
            deviceName,
            manufacturer,
            model,
            serialNumber,
            isActive != null ? isActive : false,
            domainCapabilities,
            domainStatus,
            createdAt,
            updatedAt
        );
    }
    
    /**
     * Creates a DTO from a domain entity.
     * @param scanner the domain entity to convert
     * @return SharedScannerDTO representing the domain entity
     */
    public static SharedScannerDTO fromEntity(DomainEntityScanner scanner) {
        if (scanner == null) {
            return null;
        }
        
        SharedScannerCapabilitiesDTO capabilitiesDTO = null;
        if (scanner.getCapabilities() != null) {
            capabilitiesDTO = SharedScannerCapabilitiesDTO.fromEntity(scanner.getCapabilities());
        }
        
        SharedScannerStatusDTO statusDTO = null;
        if (scanner.getStatus() != null) {
            statusDTO = SharedScannerStatusDTO.fromEntity(scanner.getStatus());
        }
        
        return new SharedScannerDTO(
            scanner.getScannerId(),
            scanner.getDeviceName(),
            scanner.getManufacturer(),
            scanner.getModel(),
            scanner.getSerialNumber(),
            scanner.isActive(),
            capabilitiesDTO,
            statusDTO,
            scanner.getCreatedAt(),
            scanner.getUpdatedAt()
        );
    }
    
    // Getters and Setters
    public SharedValueScannerId getScannerId() { return scannerId; }
    public void setScannerId(SharedValueScannerId scannerId) { this.scannerId = scannerId; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public SharedScannerCapabilitiesDTO getCapabilities() { return capabilities; }
    public void setCapabilities(SharedScannerCapabilitiesDTO capabilities) { this.capabilities = capabilities; }
    
    public SharedScannerStatusDTO getStatus() { return status; }
    public void setStatus(SharedScannerStatusDTO status) { this.status = status; }
    
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerDTO)) return false;
        SharedScannerDTO that = (SharedScannerDTO) o;
        return Objects.equals(scannerId, that.scannerId) &&
               Objects.equals(deviceName, that.deviceName) &&
               Objects.equals(manufacturer, that.manufacturer) &&
               Objects.equals(model, that.model) &&
               Objects.equals(serialNumber, that.serialNumber) &&
               Objects.equals(isActive, that.isActive) &&
               Objects.equals(capabilities, that.capabilities) &&
               Objects.equals(status, that.status) &&
               Objects.equals(createdAt, that.createdAt) &&
               Objects.equals(updatedAt, that.updatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(scannerId, deviceName, manufacturer, model, serialNumber,
                          isActive, capabilities, status, createdAt, updatedAt);
    }
    
    @Override
    public String toString() {
        return "SharedScannerDTO{" +
               "scannerId=" + scannerId +
               ", deviceName='" + deviceName + '\'' +
               ", manufacturer='" + manufacturer + '\'' +
               ", model='" + model + '\'' +
               ", serialNumber='" + serialNumber + '\'' +
               ", isActive=" + isActive +
               ", capabilities=" + capabilities +
               ", status=" + status +
               ", createdAt=" + createdAt +
               ", updatedAt=" + updatedAt +
               '}';
    }
}