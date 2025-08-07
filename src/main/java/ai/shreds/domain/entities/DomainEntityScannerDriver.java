package ai.shreds.domain.entities;

import ai.shreds.shared.enums.SharedEnumDriverType;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.shared.dtos.SharedDTOScannerDriverDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Domain entity representing a scanner driver.
 */
public class DomainEntityScannerDriver {
    private final String driverId;
    private final SharedEnumDriverType driverType;
    private final String version;
    private final String vendor;
    private final String installationPath;
    private final List<SharedEnumProtocolType> supportedProtocols;

    public DomainEntityScannerDriver(String driverId, SharedEnumDriverType driverType, 
                                    String version, String vendor, String installationPath, 
                                    List<SharedEnumProtocolType> supportedProtocols) {
        this.driverId = Objects.requireNonNull(driverId, "Driver ID cannot be null");
        this.driverType = Objects.requireNonNull(driverType, "Driver type cannot be null");
        this.version = Objects.requireNonNull(version, "Version cannot be null");
        this.vendor = Objects.requireNonNull(vendor, "Vendor cannot be null");
        this.installationPath = Objects.requireNonNull(installationPath, "Installation path cannot be null");
        this.supportedProtocols = supportedProtocols != null ? new ArrayList<>(supportedProtocols) : new ArrayList<>();
    }

    public boolean supportsProtocol(SharedEnumProtocolType protocol) {
        return supportedProtocols.contains(protocol);
    }

    public boolean isCompatibleWith(DomainEntityScanner scanner) {
        if (scanner == null) {
            return false;
        }
        
        // Basic compatibility check - can be extended with more sophisticated logic
        // For now, we assume compatibility based on driver type and supported protocols
        return !supportedProtocols.isEmpty();
    }

    public String getVersionInfo() {
        return vendor + " " + driverType.name() + " Driver v" + version;
    }

    public boolean isInstalled() {
        // Basic check - in a real implementation, this would verify the driver files exist
        return installationPath != null && !installationPath.trim().isEmpty();
    }

    public boolean supportsDriverType(SharedEnumDriverType type) {
        return this.driverType == type;
    }

    public SharedDTOScannerDriverDTO toDTO() {
        return SharedDTOScannerDriverDTO.fromEntity(this);
    }

    public static DomainEntityScannerDriver fromDTO(SharedDTOScannerDriverDTO dto) {
        if (dto == null) {
            return null;
        }
        return dto.toEntity();
    }

    // Getters
    public String getDriverId() { return driverId; }
    public SharedEnumDriverType getDriverType() { return driverType; }
    public String getVersion() { return version; }
    public String getVendor() { return vendor; }
    public String getInstallationPath() { return installationPath; }
    public List<SharedEnumProtocolType> getSupportedProtocols() { return new ArrayList<>(supportedProtocols); }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainEntityScannerDriver that = (DomainEntityScannerDriver) other;
        return Objects.equals(driverId, that.driverId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverId);
    }

    @Override
    public String toString() {
        return "DomainEntityScannerDriver{" +
               "driverId='" + driverId + '\'' +
               ", driverType=" + driverType +
               ", version='" + version + '\'' +
               ", vendor='" + vendor + '\'' +
               ", supportedProtocols=" + supportedProtocols +
               '}';
    }
}