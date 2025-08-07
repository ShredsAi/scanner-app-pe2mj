package ai.shreds.adapters.primary;

import ai.shreds.application.ports.ApplicationInputPortScannerDiscovery;
import ai.shreds.shared.dtos.SharedHardwareEvent;
import ai.shreds.shared.dtos.SharedScannerDTO;
import ai.shreds.adapters.exceptions.AdapterExceptionEventListenerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Primary adapter that listens for hardware events from the operating system.
 * This component handles device insertion and removal events to trigger
 * immediate discovery cycles or handle device disconnections.
 */
@Component
public class AdapterHardwareEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterHardwareEventListener.class);
    
    private static final String DEVICE_ARRIVAL = "DEVICE_ARRIVAL";
    private static final String DEVICE_REMOVAL = "DEVICE_REMOVAL";
    private static final String DEVICE_REMOVAL_COMPLETE = "DEVICE_REMOVAL_COMPLETE";
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationInputPortScannerDiscovery scannerDiscoveryService;
    
    @Autowired
    public AdapterHardwareEventListener(
            ApplicationEventPublisher applicationEventPublisher,
            ApplicationInputPortScannerDiscovery scannerDiscoveryService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.scannerDiscoveryService = scannerDiscoveryService;
    }
    
    /**
     * Handles hardware events from the operating system.
     * This method processes device insertion and removal events.
     * 
     * @param event the hardware event containing event type and device information
     */
    @EventListener
    public void onHardwareEvent(SharedHardwareEvent event) {
        try {
            logger.debug("Received hardware event: {} for device: {}", 
                        event.getEventType(), event.getDeviceId());
            
            switch (event.getEventType()) {
                case DEVICE_ARRIVAL:
                    handleDeviceInsertion(event.getDeviceId());
                    break;
                case DEVICE_REMOVAL:
                case DEVICE_REMOVAL_COMPLETE:
                    handleDeviceRemoval(event.getDeviceId());
                    break;
                default:
                    logger.debug("Unhandled hardware event type: {}", event.getEventType());
                    break;
            }
            
        } catch (Exception e) {
            logger.error("Error processing hardware event: {} for device: {}", 
                        event.getEventType(), event.getDeviceId(), e);
            throw new AdapterExceptionEventListenerException(
                "Failed to process hardware event: " + event.getEventType(), e);
        }
    }
    
    /**
     * Handles device insertion events by triggering immediate discovery.
     * 
     * @param deviceId the identifier of the inserted device
     */
    public void handleDeviceInsertion(String deviceId) {
        try {
            logger.info("Device insertion detected for device: {}, triggering discovery", deviceId);
            
            // Trigger immediate discovery cycle for new device
            scannerDiscoveryService.handleHardwareEvent(DEVICE_ARRIVAL, deviceId);
            
            // Also run a full discovery to ensure we catch the new device
            List<SharedScannerDTO> discoveredScanners = scannerDiscoveryService.discoverScanners();
            logger.debug("Discovery after device insertion found {} scanners", 
                        discoveredScanners != null ? discoveredScanners.size() : 0);
            
            logger.debug("Device insertion handling completed for device: {}", deviceId);
            
        } catch (Exception e) {
            logger.error("Error handling device insertion for device: {}", deviceId, e);
            throw new AdapterExceptionEventListenerException(
                "Failed to handle device insertion for device: " + deviceId, e);
        }
    }
    
    /**
     * Handles device removal events by updating device status and triggering cleanup.
     * 
     * @param deviceId the identifier of the removed device
     */
    public void handleDeviceRemoval(String deviceId) {
        try {
            logger.info("Device removal detected for device: {}, updating status", deviceId);
            
            // Handle the device removal event
            scannerDiscoveryService.handleHardwareEvent(DEVICE_REMOVAL, deviceId);
            
            // Synchronize cached devices to remove disconnected ones
            scannerDiscoveryService.synchronizeCachedDevices();
            
            logger.debug("Device removal handling completed for device: {}", deviceId);
            
        } catch (Exception e) {
            logger.error("Error handling device removal for device: {}", deviceId, e);
            throw new AdapterExceptionEventListenerException(
                "Failed to handle device removal for device: " + deviceId, e);
        }
    }
}