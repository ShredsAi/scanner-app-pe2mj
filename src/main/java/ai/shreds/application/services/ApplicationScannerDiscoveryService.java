package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationInputPortScannerDiscovery;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.application.ports.ApplicationOutputPortCacheManager;
import ai.shreds.domain.services.DomainServiceScannerDiscovery;
import ai.shreds.domain.services.DomainServiceDriverCommunication;
import ai.shreds.shared.dtos.SharedScannerDTO;
import ai.shreds.shared.dtos.SharedScannerDiscoveredEvent;
import ai.shreds.shared.dtos.SharedScannerConnectedEvent;
import ai.shreds.shared.dtos.SharedScannerDisconnectedEvent;
import ai.shreds.shared.dtos.SharedDeviceConnectionDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.UUID;

/**
 * Application service for scanner discovery operations.
 * Implements the scanner discovery input port and coordinates with domain services.
 */
@Service
public class ApplicationScannerDiscoveryService implements ApplicationInputPortScannerDiscovery {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationScannerDiscoveryService.class);

    private final DomainServiceScannerDiscovery domainScannerDiscoveryService;
    private final DomainServiceDriverCommunication domainDriverCommunicationService;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    private final ApplicationOutputPortCacheManager cacheManager;

    @Autowired
    public ApplicationScannerDiscoveryService(
            DomainServiceScannerDiscovery domainScannerDiscoveryService,
            DomainServiceDriverCommunication domainDriverCommunicationService,
            ApplicationOutputPortEventPublisher eventPublisher,
            ApplicationOutputPortCacheManager cacheManager) {
        this.domainScannerDiscoveryService = domainScannerDiscoveryService;
        this.domainDriverCommunicationService = domainDriverCommunicationService;
        this.eventPublisher = eventPublisher;
        this.cacheManager = cacheManager;
    }

    @Override
    public List<SharedScannerDTO> discoverScanners() {
        logger.info("Starting scanner discovery process");
        
        try {
            // Get previously cached scanners for comparison
            List<SharedScannerDTO> previousScanners = cacheManager.getAllCachedScanners();
            
            // Discover current scanners through domain service
            List<SharedScannerDTO> currentScanners = domainScannerDiscoveryService.discoverScanners()
                    .stream()
                    .map(scanner -> scanner.toDTO())
                    .collect(Collectors.toList());
            
            // Cache the discovered scanners
            currentScanners.forEach(cacheManager::cacheScannerInformation);
            
            // Detect and publish state changes
            detectStateChanges(previousScanners, currentScanners);
            
            // Publish discovery events for new scanners
            publishDiscoveryEvents(currentScanners, previousScanners);
            
            logger.info("Scanner discovery completed. Found {} scanners", currentScanners.size());
            return currentScanners;
            
        } catch (Exception e) {
            logger.error("Error during scanner discovery", e);
            throw new RuntimeException("Scanner discovery failed", e);
        }
    }

    @Override
    public void pollConnectedDevices() {
        logger.debug("Polling connected devices for status updates");
        
        try {
            // Get all cached scanners
            List<SharedScannerDTO> cachedScanners = cacheManager.getAllCachedScanners();
            
            // Poll each device through domain service
            domainScannerDiscoveryService.pollConnectedDevices();
            
            // Update cache with latest status
            List<SharedScannerDTO> updatedScanners = domainScannerDiscoveryService.discoverScanners()
                    .stream()
                    .map(scanner -> scanner.toDTO())
                    .collect(Collectors.toList());
            
            // Detect disconnected devices
            detectStateChanges(cachedScanners, updatedScanners);
            
            logger.debug("Device polling completed");
            
        } catch (Exception e) {
            logger.error("Error during device polling", e);
        }
    }

    @Override
    public void handleHardwareEvent(String eventType, String deviceId) {
        logger.info("Handling hardware event: {} for device: {}", eventType, deviceId);
        
        try {
            // Delegate to domain service
            domainScannerDiscoveryService.handleHardwareEvent(eventType, deviceId);
            
            // Trigger immediate discovery if device was added
            if ("DEVICE_ARRIVAL".equals(eventType) || "DEVICE_INSERTION".equals(eventType)) {
                discoverScanners();
            }
            // Handle device removal
            else if ("DEVICE_REMOVAL".equals(eventType) || "DEVICE_DISCONNECTION".equals(eventType)) {
                handleDeviceRemoval(deviceId);
            }
            
        } catch (Exception e) {
            logger.error("Error handling hardware event: {} for device: {}", eventType, deviceId, e);
        }
    }

    @Override
    public void synchronizeCachedDevices() {
        logger.debug("Synchronizing cached devices");
        
        try {
            // Delegate to domain service
            domainScannerDiscoveryService.synchronizeCachedDevices();
            
            // Evict disconnected devices from cache
            cacheManager.evictDisconnectedDevices();
            
            logger.debug("Cache synchronization completed");
            
        } catch (Exception e) {
            logger.error("Error during cache synchronization", e);
        }
    }

    /**
     * Publishes discovery events for newly found scanners.
     */
    private void publishDiscoveryEvents(List<SharedScannerDTO> currentScanners, List<SharedScannerDTO> previousScanners) {
        Map<String, SharedScannerDTO> previousMap = previousScanners.stream()
                .collect(Collectors.toMap(s -> s.getScannerId().toString(), s -> s));
        
        currentScanners.stream()
                .filter(scanner -> !previousMap.containsKey(scanner.getScannerId().toString()))
                .forEach(scanner -> {
                    SharedScannerDiscoveredEvent event = new SharedScannerDiscoveredEvent(scanner);
                    event.setCorrelationId(UUID.randomUUID().toString());
                    eventPublisher.publishScannerDiscoveredEvent(event);
                    logger.info("Published scanner discovered event for: {}", scanner.getDeviceName());
                });
    }

    /**
     * Detects state changes between previous and current scanner states.
     */
    private void detectStateChanges(List<SharedScannerDTO> previousScanners, List<SharedScannerDTO> currentScanners) {
        Map<String, SharedScannerDTO> currentMap = currentScanners.stream()
                .collect(Collectors.toMap(s -> s.getScannerId().toString(), s -> s));
        
        Map<String, SharedScannerDTO> previousMap = previousScanners.stream()
                .collect(Collectors.toMap(s -> s.getScannerId().toString(), s -> s));
        
        // Detect newly connected devices
        currentScanners.stream()
                .filter(scanner -> !previousMap.containsKey(scanner.getScannerId().toString()))
                .forEach(scanner -> {
                    SharedDeviceConnectionDTO connectionDetails = new SharedDeviceConnectionDTO();
                    connectionDetails.setScannerId(scanner.getScannerId());
                    connectionDetails.setEstablishedAt(Instant.now());
                    connectionDetails.setConnected(true);
                    
                    SharedScannerConnectedEvent event = new SharedScannerConnectedEvent(
                            scanner.getScannerId().toString(), connectionDetails);
                    event.setCorrelationId(UUID.randomUUID().toString());
                    eventPublisher.publishScannerConnectedEvent(event);
                });
        
        // Detect disconnected devices
        previousScanners.stream()
                .filter(scanner -> !currentMap.containsKey(scanner.getScannerId().toString()))
                .forEach(scanner -> {
                    SharedScannerDisconnectedEvent event = new SharedScannerDisconnectedEvent(
                            scanner.getScannerId().toString(), "Device no longer detected");
                    event.setCorrelationId(UUID.randomUUID().toString());
                    eventPublisher.publishScannerDisconnectedEvent(event);
                });
    }

    /**
     * Handles device removal by cleaning up cache and publishing events.
     */
    private void handleDeviceRemoval(String deviceId) {
        logger.info("Handling device removal for: {}", deviceId);
        
        // Publish disconnection event
        SharedScannerDisconnectedEvent event = new SharedScannerDisconnectedEvent(
                deviceId, "Hardware removal detected");
        event.setCorrelationId(UUID.randomUUID().toString());
        eventPublisher.publishScannerDisconnectedEvent(event);
        
        // Note: Cache cleanup will be handled by the evictDisconnectedDevices method
    }
}