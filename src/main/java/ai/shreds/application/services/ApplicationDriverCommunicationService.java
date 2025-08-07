package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.services.DomainServiceDriverCommunication;
import ai.shreds.shared.dtos.SharedScannerDTO;
import ai.shreds.shared.dtos.SharedScannerCapabilitiesDTO;
import ai.shreds.shared.dtos.SharedDriverErrorEvent;
import ai.shreds.shared.enums.SharedEnumDriverType;
import ai.shreds.shared.value_objects.SharedValueScannerId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * Application service for driver communication operations.
 * Handles communication with TWAIN and WIA drivers with retry logic and error handling.
 */
@Service
public class ApplicationDriverCommunicationService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDriverCommunicationService.class);
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_DELAY = 1000L; // 1 second
    private static final double BACKOFF_MULTIPLIER = 2.0;

    private final DomainServiceDriverCommunication domainDriverCommunicationService;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    private final RetryTemplate retryTemplate;

    @Autowired
    public ApplicationDriverCommunicationService(
            DomainServiceDriverCommunication domainDriverCommunicationService,
            ApplicationOutputPortEventPublisher eventPublisher) {
        this.domainDriverCommunicationService = domainDriverCommunicationService;
        this.eventPublisher = eventPublisher;
        this.retryTemplate = createRetryTemplate();
    }

    /**
     * Initializes all available drivers (TWAIN and WIA).
     */
    public void initializeDrivers() {
        logger.info("Initializing scanner drivers");
        
        try {
            retryWithBackoff(() -> {
                domainDriverCommunicationService.initializeDrivers();
                return null;
            });
            
            logger.info("Scanner drivers initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize scanner drivers", e);
            handleDriverError(e, SharedEnumDriverType.TWAIN); // Default to TWAIN for initialization errors
        }
    }

    /**
     * Enumerates all TWAIN-compatible devices.
     * @return list of discovered TWAIN scanners
     */
    public List<SharedScannerDTO> enumerateTwainDevices() {
        logger.debug("Enumerating TWAIN devices");
        
        try {
            return retryWithBackoff(() -> {
                return domainDriverCommunicationService.enumerateTwainDevices()
                        .stream()
                        .map(scanner -> scanner.toDTO())
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            logger.error("Failed to enumerate TWAIN devices", e);
            handleDriverError(e, SharedEnumDriverType.TWAIN);
            return List.of(); // Return empty list on failure
        }
    }

    /**
     * Enumerates all WIA-compatible devices.
     * @return list of discovered WIA scanners
     */
    public List<SharedScannerDTO> enumerateWiaDevices() {
        logger.debug("Enumerating WIA devices");
        
        try {
            return retryWithBackoff(() -> {
                return domainDriverCommunicationService.enumerateWiaDevices()
                        .stream()
                        .map(scanner -> scanner.toDTO())
                        .collect(Collectors.toList());
            });
        } catch (Exception e) {
            logger.error("Failed to enumerate WIA devices", e);
            handleDriverError(e, SharedEnumDriverType.WIA);
            return List.of(); // Return empty list on failure
        }
    }

    /**
     * Retrieves detailed capabilities for a specific scanner device.
     * @param scannerId the unique identifier of the scanner
     * @return scanner capabilities DTO
     */
    public SharedScannerCapabilitiesDTO retrieveDeviceCapabilities(SharedValueScannerId scannerId) {
        logger.debug("Retrieving capabilities for scanner: {}", scannerId);
        
        try {
            return retryWithBackoff(() -> {
                // Convert scannerId to string for domain service call
                String deviceId = scannerId.getDeviceId();
                var capabilities = domainDriverCommunicationService.retrieveDeviceCapabilities(
                        deviceId, 
                        ai.shreds.shared.enums.SharedEnumProtocolType.TWAIN // Default to TWAIN
                );
                
                // Convert domain capabilities to DTO
                // This would typically involve mapping the Map<String, Object> to SharedScannerCapabilitiesDTO
                SharedScannerCapabilitiesDTO capabilitiesDTO = new SharedScannerCapabilitiesDTO();
                // Map capabilities from the domain response
                mapCapabilitiesToDTO(capabilities, capabilitiesDTO);
                
                return capabilitiesDTO;
            });
        } catch (Exception e) {
            logger.error("Failed to retrieve capabilities for scanner: {}", scannerId, e);
            handleDriverError(e, SharedEnumDriverType.TWAIN);
            return new SharedScannerCapabilitiesDTO(); // Return empty capabilities on failure
        }
    }

    /**
     * Tests the connection to a specific scanner device.
     * @param scannerId the unique identifier of the scanner
     * @return true if connection test is successful, false otherwise
     */
    public Boolean testDeviceConnection(SharedValueScannerId scannerId) {
        logger.debug("Testing connection for scanner: {}", scannerId);
        
        try {
            return retryWithBackoff(() -> {
                return domainDriverCommunicationService.testDeviceConnection(scannerId);
            });
        } catch (Exception e) {
            logger.error("Connection test failed for scanner: {}", scannerId, e);
            handleDriverError(e, SharedEnumDriverType.TWAIN);
            return false;
        }
    }

    /**
     * Handles driver communication errors by publishing error events.
     */
    private void handleDriverError(Exception error, SharedEnumDriverType driverType) {
        logger.error("Driver error occurred for type: {}", driverType, error);
        
        try {
            SharedDriverErrorEvent errorEvent = new SharedDriverErrorEvent(
                    "UNKNOWN_SCANNER", // Scanner ID not available in this context
                    "DRIVER_COMMUNICATION_ERROR",
                    error.getMessage() != null ? error.getMessage() : "Unknown driver error",
                    driverType
            );
            errorEvent.setCorrelationId(UUID.randomUUID().toString());
            
            eventPublisher.publishDriverErrorEvent(errorEvent);
        } catch (Exception publishError) {
            logger.error("Failed to publish driver error event", publishError);
        }
    }

    /**
     * Executes an operation with retry logic and exponential backoff.
     */
    private <T> T retryWithBackoff(Callable<T> operation) throws Exception {
        return retryTemplate.execute(context -> {
            try {
                return operation.call();
            } catch (Exception e) {
                logger.warn("Operation failed, attempt {}: {}", context.getRetryCount() + 1, e.getMessage());
                throw e;
            }
        });
    }

    /**
     * Creates a retry template with exponential backoff policy.
     */
    private RetryTemplate createRetryTemplate() {
        RetryTemplate template = new RetryTemplate();
        
        // Configure retry policy
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(MAX_RETRY_ATTEMPTS);
        template.setRetryPolicy(retryPolicy);
        
        // Configure backoff policy
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(INITIAL_BACKOFF_DELAY);
        backOffPolicy.setMultiplier(BACKOFF_MULTIPLIER);
        backOffPolicy.setMaxInterval(10000L); // Max 10 seconds
        template.setBackOffPolicy(backOffPolicy);
        
        return template;
    }

    /**
     * Maps domain capabilities to DTO format.
     */
    private void mapCapabilitiesToDTO(Map<String, Object> capabilities, SharedScannerCapabilitiesDTO dto) {
        // Implementation would map specific capability fields
        // This is a placeholder for the actual mapping logic
        if (capabilities.containsKey("maxResolution")) {
            dto.setMaxResolution((Integer) capabilities.get("maxResolution"));
        }
        if (capabilities.containsKey("minResolution")) {
            dto.setMinResolution((Integer) capabilities.get("minResolution"));
        }
        if (capabilities.containsKey("duplexSupport")) {
            dto.setDuplexSupport((Boolean) capabilities.get("duplexSupport"));
        }
        if (capabilities.containsKey("maxScanWidth")) {
            dto.setMaxScanWidth((Integer) capabilities.get("maxScanWidth"));
        }
        if (capabilities.containsKey("maxScanHeight")) {
            dto.setMaxScanHeight((Integer) capabilities.get("maxScanHeight"));
        }
        
        // Set default values for collections if not present
        if (dto.getSupportedResolutions() == null) {
            dto.setSupportedResolutions(List.of());
        }
        if (dto.getColorModes() == null) {
            dto.setColorModes(List.of());
        }
        if (dto.getPaperSizes() == null) {
            dto.setPaperSizes(List.of());
        }
    }
}