package ai.shreds.adapters.primary;

import ai.shreds.application.ports.ApplicationInputPortScannerDiscovery;
import ai.shreds.adapters.exceptions.AdapterExceptionSchedulerException;
import ai.shreds.shared.dtos.SharedScannerDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Primary adapter that handles scheduled scanner discovery operations.
 * This component is responsible for triggering periodic scanner discovery cycles
 * and handling scheduled discovery tasks.
 */
@Component
public class AdapterScannerDiscoveryScheduler {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterScannerDiscoveryScheduler.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;
    private final ApplicationInputPortScannerDiscovery scannerDiscoveryService;
    
    @Autowired
    public AdapterScannerDiscoveryScheduler(
            ApplicationEventPublisher applicationEventPublisher,
            ApplicationInputPortScannerDiscovery scannerDiscoveryService) {
        this.applicationEventPublisher = applicationEventPublisher;
        this.scannerDiscoveryService = scannerDiscoveryService;
    }
    
    /**
     * Initiates the scanner discovery process.
     * This method can be called manually to trigger discovery outside of scheduled intervals.
     */
    public void discoverScanners() {
        try {
            logger.debug("Manual scanner discovery initiated");
            List<SharedScannerDTO> discoveredScanners = scannerDiscoveryService.discoverScanners();
            logger.info("Manual scanner discovery completed successfully. Found {} scanners", 
                       discoveredScanners != null ? discoveredScanners.size() : 0);
        } catch (Exception e) {
            logger.error("Error during manual scanner discovery", e);
            throw new AdapterExceptionSchedulerException("Failed to execute manual scanner discovery", e);
        }
    }
    
    /**
     * Handles scheduled discovery operations.
     * This method is automatically triggered at configured intervals to maintain
     * up-to-date information about connected scanner devices.
     */
    @Scheduled(fixedDelayString = "${scanner.discovery.polling-interval:30000}")
    public void handleScheduledDiscovery() {
        try {
            logger.debug("Scheduled scanner discovery cycle starting");
            
            // Discover new scanners
            List<SharedScannerDTO> discoveredScanners = scannerDiscoveryService.discoverScanners();
            logger.debug("Discovered {} scanners in this cycle", 
                        discoveredScanners != null ? discoveredScanners.size() : 0);
            
            // Poll existing connected devices for status updates
            scannerDiscoveryService.pollConnectedDevices();
            
            // Synchronize cached devices
            scannerDiscoveryService.synchronizeCachedDevices();
            
            logger.debug("Scheduled scanner discovery cycle completed successfully");
            
        } catch (Exception e) {
            logger.error("Error during scheduled scanner discovery cycle", e);
            // Don't throw exception here to prevent stopping the scheduler
            // The error will be logged and the next cycle will continue
        }
    }
}