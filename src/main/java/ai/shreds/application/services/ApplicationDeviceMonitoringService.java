package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationOutputPortCacheManager;
import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.domain.services.DomainServiceDeviceMonitoring;
import ai.shreds.domain.entities.DomainEntityScannerStatus;
import ai.shreds.shared.dtos.SharedScannerStatusDTO;
import ai.shreds.shared.dtos.SharedScannerConnectedEvent;
import ai.shreds.shared.dtos.SharedScannerDisconnectedEvent;
import ai.shreds.shared.dtos.SharedDeviceConnectionDTO;
import ai.shreds.shared.value_objects.SharedValueScannerId;
import ai.shreds.shared.enums.SharedEnumConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Application service for device monitoring operations.
 * Handles continuous monitoring of scanner device status and connection state.
 */
@Service
public class ApplicationDeviceMonitoringService {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationDeviceMonitoringService.class);

    private final DomainServiceDeviceMonitoring domainMonitoringService;
    private final ApplicationOutputPortCacheManager cacheManager;
    private final ApplicationOutputPortEventPublisher eventPublisher;
    private final ScheduledExecutorService monitoringExecutor;
    
    @Value("${scanner.monitoring.interval:30}")
    private long monitoringIntervalSeconds;
    
    @Value("${scanner.monitoring.heartbeat.interval:10}")
    private long heartbeatIntervalSeconds;

    @Autowired
    public ApplicationDeviceMonitoringService(
            DomainServiceDeviceMonitoring domainMonitoringService,
            ApplicationOutputPortCacheManager cacheManager,
            ApplicationOutputPortEventPublisher eventPublisher) {
        this.domainMonitoringService = domainMonitoringService;
        this.cacheManager = cacheManager;
        this.eventPublisher = eventPublisher;
        this.monitoringExecutor = Executors.newScheduledThreadPool(2);
    }

    /**
     * Starts the device monitoring process.
     * Initializes scheduled tasks for device status checking and heartbeat monitoring.
     */
    @PostConstruct
    public void startMonitoring() {
        logger.info("Starting device monitoring service");
        
        try {
            // Start domain monitoring service
            domainMonitoringService.startMonitoring();
            
            // Schedule periodic device status checks
            monitoringExecutor.scheduleAtFixedRate(
                    this::performDeviceStatusCheck,
                    0,
                    monitoringIntervalSeconds,
                    TimeUnit.SECONDS
            );
            
            // Schedule heartbeat checks
            scheduleHeartbeatCheck();
            
            logger.info("Device monitoring started with interval: {} seconds", monitoringIntervalSeconds);
        } catch (Exception e) {
            logger.error("Failed to start device monitoring", e);
        }
    }

    /**
     * Stops the device monitoring process.
     * Gracefully shuts down all monitoring tasks and releases resources.
     */
    @PreDestroy
    public void stopMonitoring() {
        logger.info("Stopping device monitoring service");
        
        try {
            // Stop domain monitoring service
            domainMonitoringService.stopMonitoring();
            
            // Shutdown executor service
            monitoringExecutor.shutdown();
            if (!monitoringExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                monitoringExecutor.shutdownNow();
            }
            
            logger.info("Device monitoring stopped successfully");
        } catch (Exception e) {
            logger.error("Error stopping device monitoring", e);
        }
    }

    /**
     * Checks the status of a specific scanner device.
     * @param scannerId the unique identifier of the scanner
     * @return current status of the scanner
     */
    public SharedScannerStatusDTO checkDeviceStatus(SharedValueScannerId scannerId) {
        logger.debug("Checking status for scanner: {}", scannerId);
        
        try {
            // Get scanner from cache first
            var cachedScanner = cacheManager.retrieveCachedScanner(scannerId.toString());
            if (cachedScanner.isEmpty()) {
                logger.warn("Scanner not found in cache: {}", scannerId);
                return createOfflineStatus();
            }
            
            // Check status through domain service
            var scanner = cachedScanner.get().toEntity();
            DomainEntityScannerStatus domainStatus = domainMonitoringService.checkDeviceStatus(scanner);
            
            // Convert to DTO
            SharedScannerStatusDTO statusDTO = domainStatus.toDTO();
            
            // Detect state changes
            var previousStatus = cachedScanner.get().getStatus();
            detectStateChanges(previousStatus, statusDTO);
            
            return statusDTO;
            
        } catch (Exception e) {
            logger.error("Error checking device status for scanner: {}", scannerId, e);
            return createErrorStatus(e.getMessage());
        }
    }

    /**
     * Detects state changes between previous and current scanner status.
     */
    public void detectStateChanges(SharedScannerStatusDTO previousStatus, SharedScannerStatusDTO currentStatus) {
        if (previousStatus == null || currentStatus == null) {
            return;
        }
        
        try {
            // Check for connection state changes
            if (!previousStatus.getConnectionState().equals(currentStatus.getConnectionState())) {
                handleConnectionStateChange(previousStatus, currentStatus);
            }
            
            // Check for availability state changes
            if (!previousStatus.getAvailability().equals(currentStatus.getAvailability())) {
                logger.info("Availability state changed from {} to {}", 
                        previousStatus.getAvailability(), currentStatus.getAvailability());
            }
            
            // Check for error state changes
            if (!previousStatus.getErrorState().equals(currentStatus.getErrorState())) {
                logger.warn("Error state changed from {} to {}", 
                        previousStatus.getErrorState(), currentStatus.getErrorState());
            }
            
        } catch (Exception e) {
            logger.error("Error detecting state changes", e);
        }
    }

    /**
     * Publishes state change events based on the type of change detected.
     */
    public void publishStateChangeEvents(SharedValueScannerId scannerId, String changeType) {
        logger.debug("Publishing state change event: {} for scanner: {}", changeType, scannerId);
        
        try {
            String correlationId = UUID.randomUUID().toString();
            
            switch (changeType.toUpperCase()) {
                case "CONNECTED":
                    SharedDeviceConnectionDTO connectionDetails = createConnectionDetails(scannerId);
                    SharedScannerConnectedEvent connectedEvent = new SharedScannerConnectedEvent(
                            scannerId.toString(), connectionDetails);
                    connectedEvent.setCorrelationId(correlationId);
                    eventPublisher.publishScannerConnectedEvent(connectedEvent);
                    break;
                    
                case "DISCONNECTED":
                    SharedScannerDisconnectedEvent disconnectedEvent = new SharedScannerDisconnectedEvent(
                            scannerId.toString(), "Device monitoring detected disconnection");
                    disconnectedEvent.setCorrelationId(correlationId);
                    eventPublisher.publishScannerDisconnectedEvent(disconnectedEvent);
                    break;
                    
                default:
                    logger.debug("No specific event handler for change type: {}", changeType);
            }
            
        } catch (Exception e) {
            logger.error("Error publishing state change event: {} for scanner: {}", changeType, scannerId, e);
        }
    }

    /**
     * Performs periodic device status checks for all cached scanners.
     */
    private void performDeviceStatusCheck() {
        logger.debug("Performing periodic device status check");
        
        try {
            var cachedScanners = cacheManager.getAllCachedScanners();
            
            for (var scanner : cachedScanners) {
                try {
                    checkDeviceStatus(scanner.getScannerId());
                } catch (Exception e) {
                    logger.error("Error checking status for scanner: {}", scanner.getScannerId(), e);
                }
            }
            
            logger.debug("Completed device status check for {} scanners", cachedScanners.size());
        } catch (Exception e) {
            logger.error("Error during periodic device status check", e);
        }
    }

    /**
     * Schedules heartbeat checks for connected devices.
     */
    private void scheduleHeartbeatCheck() {
        monitoringExecutor.scheduleAtFixedRate(
                this::performHeartbeatCheck,
                heartbeatIntervalSeconds,
                heartbeatIntervalSeconds,
                TimeUnit.SECONDS
        );
    }

    /**
     * Performs heartbeat checks to detect unresponsive devices.
     */
    private void performHeartbeatCheck() {
        logger.debug("Performing heartbeat check");
        
        try {
            var cachedScanners = cacheManager.getAllCachedScanners();
            
            for (var scanner : cachedScanners) {
                if (scanner.getStatus().getConnectionState() == SharedEnumConnectionState.CONNECTED) {
                    // Check if device is still responsive
                    long timeSinceLastSeen = System.currentTimeMillis() - 
                            scanner.getStatus().getLastSeen().toEpochMilli();
                    
                    // If no response for more than 2 minutes, mark as potentially disconnected
                    if (timeSinceLastSeen > 120000) {
                        logger.warn("Scanner {} has not responded for {} ms", 
                                scanner.getScannerId(), timeSinceLastSeen);
                        // Trigger a status check
                        checkDeviceStatus(scanner.getScannerId());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during heartbeat check", e);
        }
    }

    /**
     * Handles connection state changes by publishing appropriate events.
     */
    private void handleConnectionStateChange(SharedScannerStatusDTO previousStatus, SharedScannerStatusDTO currentStatus) {
        SharedEnumConnectionState previousState = previousStatus.getConnectionState();
        SharedEnumConnectionState currentState = currentStatus.getConnectionState();
        
        logger.info("Connection state changed from {} to {}", previousState, currentState);
        
        // Extract scanner ID from status (this would need to be available in the status)
        // For now, we'll use a placeholder approach
        SharedValueScannerId scannerId = new SharedValueScannerId("unknown", "001");
        
        if (currentState == SharedEnumConnectionState.CONNECTED && 
            previousState != SharedEnumConnectionState.CONNECTED) {
            publishStateChangeEvents(scannerId, "CONNECTED");
        } else if (currentState == SharedEnumConnectionState.DISCONNECTED && 
                   previousState == SharedEnumConnectionState.CONNECTED) {
            publishStateChangeEvents(scannerId, "DISCONNECTED");
        }
    }

    /**
     * Creates connection details for a scanner.
     */
    private SharedDeviceConnectionDTO createConnectionDetails(SharedValueScannerId scannerId) {
        SharedDeviceConnectionDTO connectionDetails = new SharedDeviceConnectionDTO();
        connectionDetails.setConnectionId(UUID.randomUUID().toString());
        connectionDetails.setScannerId(scannerId);
        connectionDetails.setProtocol(ai.shreds.shared.enums.SharedEnumProtocolType.TWAIN);
        connectionDetails.setEstablishedAt(Instant.now());
        connectionDetails.setLastHeartbeat(Instant.now());
        connectionDetails.setConnected(true);
        connectionDetails.setConnectionStatus("CONNECTED");
        return connectionDetails;
    }

    /**
     * Creates an offline status DTO.
     */
    private SharedScannerStatusDTO createOfflineStatus() {
        SharedScannerStatusDTO status = new SharedScannerStatusDTO();
        status.setConnectionState(SharedEnumConnectionState.DISCONNECTED);
        status.setAvailability(ai.shreds.shared.enums.SharedEnumAvailabilityState.OFFLINE);
        status.setLastSeen(Instant.now());
        status.setErrorState("Scanner not found in cache");
        return status;
    }

    /**
     * Creates an error status DTO.
     */
    private SharedScannerStatusDTO createErrorStatus(String errorMessage) {
        SharedScannerStatusDTO status = new SharedScannerStatusDTO();
        status.setConnectionState(SharedEnumConnectionState.ERROR);
        status.setAvailability(ai.shreds.shared.enums.SharedEnumAvailabilityState.OFFLINE);
        status.setLastSeen(Instant.now());
        status.setErrorState(errorMessage);
        status.setErrorMessage(errorMessage);
        return status;
    }
}