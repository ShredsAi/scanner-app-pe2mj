package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.entities.DomainEntityScannerDriver;
import ai.shreds.domain.ports.DomainOutputPortDriverInterface;
import ai.shreds.domain.ports.DomainOutputPortEventBus;
import ai.shreds.domain.exceptions.DomainExceptionDriverError;
import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.shared.enums.SharedEnumDriverType;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ArrayList;

/**
 * Domain service for driver communication operations.
 */
public class DomainServiceDriverCommunication {
    private final DomainOutputPortDriverInterface driverInterface;
    private final DomainOutputPortEventBus eventBus;
    private final Integer retryAttempts;

    public DomainServiceDriverCommunication(DomainOutputPortDriverInterface driverInterface,
                                           DomainOutputPortEventBus eventBus) {
        this.driverInterface = Objects.requireNonNull(driverInterface, "Driver interface cannot be null");
        this.eventBus = Objects.requireNonNull(eventBus, "Event bus cannot be null");
        this.retryAttempts = 3; // Default retry attempts
    }

    public void initializeDrivers() {
        try {
            driverInterface.initializeTwainDriver();
            System.out.println("TWAIN driver initialized successfully");
        } catch (Exception e) {
            handleDriverError(e, SharedEnumDriverType.TWAIN);
        }

        try {
            driverInterface.initializeWiaDriver();
            System.out.println("WIA driver initialized successfully");
        } catch (Exception e) {
            handleDriverError(e, SharedEnumDriverType.WIA);
        }
    }

    public List<DomainEntityScanner> enumerateTwainDevices() {
        List<DomainEntityScanner> scanners = new ArrayList<>();
        
        try {
            List<Map<String, Object>> devices = driverInterface.enumerateTwainDevices();
            for (Map<String, Object> deviceData : devices) {
                try {
                    // This would use the entity factory to create scanners
                    // For now, we'll return empty list until factory is available
                    System.out.println("Found TWAIN device: " + deviceData.get("name"));
                } catch (Exception e) {
                    System.err.println("Error processing TWAIN device: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            handleDriverError(e, SharedEnumDriverType.TWAIN);
        }
        
        return scanners;
    }

    public List<DomainEntityScanner> enumerateWiaDevices() {
        List<DomainEntityScanner> scanners = new ArrayList<>();
        
        try {
            List<Map<String, Object>> devices = driverInterface.enumerateWiaDevices();
            for (Map<String, Object> deviceData : devices) {
                try {
                    // This would use the entity factory to create scanners
                    // For now, we'll return empty list until factory is available
                    System.out.println("Found WIA device: " + deviceData.get("name"));
                } catch (Exception e) {
                    System.err.println("Error processing WIA device: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            handleDriverError(e, SharedEnumDriverType.WIA);
        }
        
        return scanners;
    }

    public Map<String, Object> retrieveDeviceCapabilities(String deviceId, SharedEnumProtocolType protocol) {
        try {
            return driverInterface.getDeviceCapabilities(deviceId, protocol);
        } catch (Exception e) {
            SharedEnumDriverType driverType = protocol == SharedEnumProtocolType.TWAIN ? 
                SharedEnumDriverType.TWAIN : SharedEnumDriverType.WIA;
            handleDriverError(e, driverType);
            return null;
        }
    }

    public boolean testDeviceConnection(SharedValueScannerId scannerId) {
        if (scannerId == null) {
            return false;
        }
        
        try {
            // Try TWAIN first
            Map<String, Object> twainCapabilities = driverInterface.getDeviceCapabilities(
                scannerId.getDeviceId(), SharedEnumProtocolType.TWAIN);
            if (twainCapabilities != null && !twainCapabilities.isEmpty()) {
                return true;
            }
            
            // Try WIA if TWAIN fails
            Map<String, Object> wiaCapabilities = driverInterface.getDeviceCapabilities(
                scannerId.getDeviceId(), SharedEnumProtocolType.WIA);
            return wiaCapabilities != null && !wiaCapabilities.isEmpty();
            
        } catch (Exception e) {
            System.err.println("Connection test failed for scanner: " + scannerId + ", error: " + e.getMessage());
            return false;
        }
    }

    public void handleDriverError(Exception error, SharedEnumDriverType driverType) throws DomainExceptionDriverError {
        String errorMessage = error.getMessage() != null ? error.getMessage() : "Unknown driver error";
        String errorCode = determineErrorCode(error);
        
        System.err.println("Driver error [" + driverType + "]: " + errorMessage);
        
        // Create and throw domain exception
        DomainExceptionDriverError driverError = new DomainExceptionDriverError(
            errorMessage, driverType, errorCode, error);
        
        // Publish error event through event bus if needed
        // eventBus.publishEvent(new DriverErrorEvent(...));
        
        throw driverError;
    }

    private void validateDriverCompatibility(DomainEntityScannerDriver driver, DomainEntityScanner scanner) {
        if (driver == null || scanner == null) {
            throw new IllegalArgumentException("Driver and scanner cannot be null");
        }
        
        if (!driver.isCompatibleWith(scanner)) {
            throw new IllegalStateException("Driver " + driver.getDriverId() + 
                " is not compatible with scanner " + scanner.getScannerId());
        }
    }

    private String determineErrorCode(Exception error) {
        if (error == null) {
            return "UNKNOWN_ERROR";
        }
        
        String className = error.getClass().getSimpleName();
        if (className.contains("Timeout")) {
            return "TIMEOUT_ERROR";
        } else if (className.contains("Connection")) {
            return "CONNECTION_ERROR";
        } else if (className.contains("NotFound")) {
            return "DEVICE_NOT_FOUND";
        } else {
            return "DRIVER_ERROR";
        }
    }

    private boolean shouldRetry(Exception error, int attemptNumber) {
        return attemptNumber < retryAttempts && isRetryableError(error);
    }

    private boolean isRetryableError(Exception error) {
        // Determine if error is retryable based on error type
        String errorMessage = error.getMessage();
        return errorMessage != null && (
            errorMessage.contains("timeout") ||
            errorMessage.contains("busy") ||
            errorMessage.contains("temporary")
        );
    }
}