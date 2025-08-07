package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumDriverType;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.domain.entities.DomainEntityScannerDriver;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

/**
 * Data Transfer Object representing a scanner driver.
 */
public class SharedDTOScannerDriverDTO {
    
    @JsonProperty("driverId")
    private String driverId;
    
    @JsonProperty("driverType")
    private SharedEnumDriverType driverType;
    
    @JsonProperty("version")
    private String version;
    
    @JsonProperty("vendor")
    private String vendor;
    
    @JsonProperty("installationPath")
    private String installationPath;
    
    @JsonProperty("supportedProtocols")
    private List<SharedEnumProtocolType> supportedProtocols;
    
    // Default constructor for JSON deserialization
    public SharedDTOScannerDriverDTO() {}
    
    // Constructor with all fields
    public SharedDTOScannerDriverDTO(String driverId, SharedEnumDriverType driverType,
                                    String version, String vendor, String installationPath,
                                    List<SharedEnumProtocolType> supportedProtocols) {
        this.driverId = driverId;
        this.driverType = driverType;
        this.version = version;
        this.vendor = vendor;
        this.installationPath = installationPath;
        this.supportedProtocols = supportedProtocols;
    }
    
    /**
     * Converts this DTO to a domain entity.
     * @return DomainEntityScannerDriver representing this DTO
     */
    public DomainEntityScannerDriver toEntity() {
        return new DomainEntityScannerDriver(
            driverId,
            driverType,
            version,
            vendor,
            installationPath,
            supportedProtocols
        );
    }
    
    /**
     * Creates a DTO from a domain entity.
     * @param driver the domain entity to convert
     * @return SharedDTOScannerDriverDTO representing the domain entity
     */
    public static SharedDTOScannerDriverDTO fromEntity(DomainEntityScannerDriver driver) {
        if (driver == null) {
            return null;
        }
        
        return new SharedDTOScannerDriverDTO(
            driver.getDriverId(),
            driver.getDriverType(),
            driver.getVersion(),
            driver.getVendor(),
            driver.getInstallationPath(),
            driver.getSupportedProtocols()
        );
    }
    
    // Getters and Setters
    public String getDriverId() { return driverId; }
    public void setDriverId(String driverId) { this.driverId = driverId; }
    
    public SharedEnumDriverType getDriverType() { return driverType; }
    public void setDriverType(SharedEnumDriverType driverType) { this.driverType = driverType; }
    
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    
    public String getVendor() { return vendor; }
    public void setVendor(String vendor) { this.vendor = vendor; }
    
    public String getInstallationPath() { return installationPath; }
    public void setInstallationPath(String installationPath) { this.installationPath = installationPath; }
    
    public List<SharedEnumProtocolType> getSupportedProtocols() { return supportedProtocols; }
    public void setSupportedProtocols(List<SharedEnumProtocolType> supportedProtocols) { this.supportedProtocols = supportedProtocols; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDTOScannerDriverDTO)) return false;
        SharedDTOScannerDriverDTO that = (SharedDTOScannerDriverDTO) o;
        return Objects.equals(driverId, that.driverId) &&
               driverType == that.driverType &&
               Objects.equals(version, that.version) &&
               Objects.equals(vendor, that.vendor) &&
               Objects.equals(installationPath, that.installationPath) &&
               Objects.equals(supportedProtocols, that.supportedProtocols);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(driverId, driverType, version, vendor, installationPath, supportedProtocols);
    }
    
    @Override
    public String toString() {
        return "SharedDTOScannerDriverDTO{" +
               "driverId='" + driverId + '\'' +
               ", driverType=" + driverType +
               ", version='" + version + '\'' +
               ", vendor='" + vendor + '\'' +
               ", installationPath='" + installationPath + '\'' +
               ", supportedProtocols=" + supportedProtocols +
               '}';
    }
}