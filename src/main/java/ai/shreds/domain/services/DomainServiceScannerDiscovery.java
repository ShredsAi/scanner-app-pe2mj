package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.ports.DomainOutputPortDriverInterface;
import ai.shreds.domain.ports.DomainOutputPortCacheRepository;
import ai.shreds.domain.ports.DomainOutputPortEventBus;
import ai.shreds.domain.value_objects.DomainValueDomainEvent;
import ai.shreds.shared.value_objects.SharedValueScannerId;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.time.Duration;

/**
 * Domain service for scanner discovery operations.
 */
public class DomainServiceScannerDiscovery {
    private final DomainOutputPortDriverInterface driverInterface;
    private final DomainOutputPortCacheRepository cacheRepository;
    private final DomainOutputPortEventBus eventBus;
    private final DomainServiceScannerEntityFactory entityFactory;

    public DomainServiceScannerDiscovery(DomainOutputPortDriverInterface driverInterface,
                                        DomainOutputPortCacheRepository cacheRepository,
                                        DomainOutputPortEventBus eventBus,
                                        DomainServiceScannerEntityFactory entityFactory) {
        this.driverInterface = Objects.requireNonNull(driverInterface, "Driver interface cannot be null");
        this.cacheRepository = Objects.requireNonNull(cacheRepository, "Cache repository cannot be null");
        this.eventBus = Objects.requireNonNull(eventBus, "Event bus cannot be null");
        this.entityFactory = Objects.requireNonNull(entityFactory, "Entity factory cannot be null");
    }

    public List<DomainEntityScanner> discoverScanners() {
        List<DomainEntityScanner> discoveredScanners = new ArrayList<>();
        
        try {
            // Initialize drivers
            driverInterface.initializeTwainDriver();
            driverInterface.initializeWiaDriver();
            
            // Discover TWAIN devices
            List<java.util.Map<String, Object>> twainDevices = driverInterface.enumerateTwainDevices();
            for (java.util.Map<String, Object> deviceData : twainDevices) {
                try {
                    DomainEntityScanner scanner = entityFactory.createFromTwainDevice(deviceData);
                    validateScannerUniqueness(scanner);
                    applyBusinessRules(scanner);
                    discoveredScanners.add(scanner);
                } catch (Exception e) {
                    // Log error but continue with other devices
                    System.err.println("Error creating scanner from TWAIN device: " + e.getMessage());
                }
            }
            
            // Discover WIA devices
            List<java.util.Map<String, Object>> wiaDevices = driverInterface.enumerateWiaDevices();
            for (java.util.Map<String, Object> deviceData : wiaDevices) {
                try {
                    DomainEntityScanner scanner = entityFactory.createFromWiaDevice(deviceData);
                    validateScannerUniqueness(scanner);
                    applyBusinessRules(scanner);
                    discoveredScanners.add(scanner);
                } catch (Exception e) {
                    // Log error but continue with other devices
                    System.err.println("Error creating scanner from WIA device: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during scanner discovery: " + e.getMessage());
        }
        
        return discoveredScanners;
    }

    public void pollConnectedDevices() {
        // Get all cached scanner keys
        Set<String> cachedKeys = cacheRepository.keys("scanner:*");
        
        for (String key : cachedKeys) {
            try {
                // Extract scanner ID from key and check if device is still connected
                String scannerId = extractScannerIdFromKey(key);
                // This would involve checking device connectivity
                // Implementation depends on specific driver capabilities
            } catch (Exception e) {
                System.err.println("Error polling device: " + key + ", error: " + e.getMessage());
            }
        }
    }

    public void handleHardwareEvent(String eventType, String deviceId) {
        if (eventType == null || deviceId == null) {
            return;
        }
        
        switch (eventType.toUpperCase()) {
            case "DEVICE_ARRIVAL":
                // Trigger immediate discovery for new device
                discoverScanners();
                break;
            case "DEVICE_REMOVAL":
                // Handle device removal
                handleDeviceRemoval(deviceId);
                break;
            default:
                System.out.println("Unknown hardware event type: " + eventType);
        }
    }

    public void synchronizeCachedDevices() {
        // Get all cached scanners
        Set<String> cachedKeys = cacheRepository.keys("scanner:*");
        Set<String> discoveredDeviceIds = new HashSet<>();
        
        // Discover current devices
        List<DomainEntityScanner> currentScanners = discoverScanners();
        for (DomainEntityScanner scanner : currentScanners) {
            discoveredDeviceIds.add(scanner.getScannerId().toString());
        }
        
        // Remove cached entries for devices that are no longer present
        for (String key : cachedKeys) {
            String deviceId = extractScannerIdFromKey(key);
            if (!discoveredDeviceIds.contains(deviceId)) {
                cacheRepository.delete(key);
            }
        }
    }

    private void validateScannerUniqueness(DomainEntityScanner scanner) {
        // Check if scanner already exists in cache
        String cacheKey = "scanner:" + scanner.getScannerId().toString();
        if (cacheRepository.get(cacheKey).isPresent()) {
            // Scanner already exists - this is normal during polling
            return;
        }
    }

    private void applyBusinessRules(DomainEntityScanner scanner) {
        // Apply domain business rules
        if (scanner.getCapabilities() == null) {
            // Set scanner as inactive if no capabilities are available
            scanner.deactivate();
        }
        
        // Additional business rules can be added here
    }

    private void handleDeviceRemoval(String deviceId) {
        String cacheKey = "scanner:" + deviceId;
        cacheRepository.delete(cacheKey);
    }

    private String extractScannerIdFromKey(String key) {
        // Extract scanner ID from cache key format "scanner:deviceId_instanceId"
        if (key.startsWith("scanner:")) {
            return key.substring(8); // Remove "scanner:" prefix
        }
        return key;
    }
}