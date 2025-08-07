package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.entities.DomainEntityScannerStatus;
import ai.shreds.domain.ports.DomainOutputPortCacheRepository;
import ai.shreds.domain.ports.DomainOutputPortEventBus;
import ai.shreds.shared.enums.SharedEnumConnectionState;
import ai.shreds.shared.enums.SharedEnumAvailabilityState;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.ArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Domain service for monitoring device status and health.
 */
public class DomainServiceDeviceMonitoring {
    private final DomainOutputPortCacheRepository cacheRepository;
    private final DomainOutputPortEventBus eventBus;
    private final Duration monitoringInterval;
    private final ScheduledExecutorService monitoringExecutor;
    private ScheduledFuture<?> monitoringTask;
    private volatile boolean isMonitoring = false;

    public DomainServiceDeviceMonitoring(Duration monitoringInterval, ScheduledExecutorService monitoringExecutor) {
        this.monitoringInterval = Objects.requireNonNull(monitoringInterval, "Monitoring interval cannot be null");
        this.monitoringExecutor = Objects.requireNonNull(monitoringExecutor, "Monitoring executor cannot be null");
        this.cacheRepository = null; // Will be injected
        this.eventBus = null; // Will be injected
    }

    public DomainServiceDeviceMonitoring(DomainOutputPortCacheRepository cacheRepository,
                                        DomainOutputPortEventBus eventBus,
                                        Duration monitoringInterval,
                                        ScheduledExecutorService monitoringExecutor) {
        this.cacheRepository = Objects.requireNonNull(cacheRepository, "Cache repository cannot be null");
        this.eventBus = Objects.requireNonNull(eventBus, "Event bus cannot be null");
        this.monitoringInterval = Objects.requireNonNull(monitoringInterval, "Monitoring interval cannot be null");
        this.monitoringExecutor = Objects.requireNonNull(monitoringExecutor, "Monitoring executor cannot be null");
    }

    public void startMonitoring() {
        if (isMonitoring) {
            System.out.println("Device monitoring is already running");
            return;
        }

        System.out.println("Starting device monitoring with interval: " + monitoringInterval);
        
        monitoringTask = monitoringExecutor.scheduleAtFixedRate(
            this::performMonitoringCycle,
            0,
            monitoringInterval.toSeconds(),
            TimeUnit.SECONDS
        );
        
        isMonitoring = true;
    }

    public void stopMonitoring() {
        if (!isMonitoring) {
            System.out.println("Device monitoring is not running");
            return;
        }

        System.out.println("Stopping device monitoring");
        
        if (monitoringTask != null) {
            monitoringTask.cancel(false);
            monitoringTask = null;
        }
        
        isMonitoring = false;
    }

    public DomainEntityScannerStatus checkDeviceStatus(DomainEntityScanner scanner) {
        if (scanner == null) {
            return null;
        }

        try {
            // Get current status
            DomainEntityScannerStatus currentStatus = scanner.getStatus();
            if (currentStatus == null) {
                // Create default status if none exists
                currentStatus = new DomainEntityScannerStatus(
                    SharedEnumConnectionState.DISCONNECTED,
                    SharedEnumAvailabilityState.OFFLINE,
                    java.time.Instant.now(),
                    null,
                    null
                );
            }

            // Check if device is still responsive
            boolean isResponsive = checkDeviceResponsiveness(scanner);
            
            if (isResponsive) {
                currentStatus.recordHeartbeat();
                if (currentStatus.getConnectionState() != SharedEnumConnectionState.CONNECTED) {
                    currentStatus.updateConnectionState(SharedEnumConnectionState.CONNECTED);
                }
                if (currentStatus.getAvailability() == SharedEnumAvailabilityState.OFFLINE) {
                    currentStatus.markAsAvailable();
                }
            } else {
                currentStatus.updateConnectionState(SharedEnumConnectionState.DISCONNECTED);
                // Don't immediately mark as offline - might be temporary
            }

            return currentStatus;

        } catch (Exception e) {
            System.err.println("Error checking device status for scanner " + scanner.getScannerId() + ": " + e.getMessage());
            return scanner.getStatus(); // Return existing status on error
        }
    }

    public List<String> detectStateChanges(DomainEntityScannerStatus previous, DomainEntityScannerStatus current) {
        List<String> changes = new ArrayList<>();
        
        if (previous == null || current == null) {
            return changes;
        }

        // Check connection state changes
        if (previous.getConnectionState() != current.getConnectionState()) {
            if (validateStateTransition(previous.getConnectionState(), current.getConnectionState())) {
                changes.add("CONNECTION_STATE_CHANGED: " + previous.getConnectionState() + " -> " + current.getConnectionState());
            }
        }

        // Check availability changes
        if (previous.getAvailability() != current.getAvailability()) {
            changes.add("AVAILABILITY_CHANGED: " + previous.getAvailability() + " -> " + current.getAvailability());
        }

        // Check error state changes
        boolean hadError = previous.getErrorState() != null;
        boolean hasError = current.getErrorState() != null;
        
        if (hadError != hasError) {
            if (hasError) {
                changes.add("ERROR_OCCURRED: " + current.getErrorState().getErrorCode());
            } else {
                changes.add("ERROR_CLEARED");
            }
        }

        return changes;
    }

    public void publishStateChangeEvents(String scannerId, String changeType) {
        if (eventBus == null) {
            System.out.println("Event bus not available, cannot publish event: " + changeType + " for scanner: " + scannerId);
            return;
        }

        try {
            // Create and publish appropriate domain event
            // This would create specific domain events based on change type
            System.out.println("Publishing state change event: " + changeType + " for scanner: " + scannerId);
            
            // Example: eventBus.publishEvent(new ScannerStateChangedEvent(scannerId, changeType));
            
        } catch (Exception e) {
            System.err.println("Error publishing state change event: " + e.getMessage());
        }
    }

    private void performMonitoringCycle() {
        try {
            System.out.println("Performing device monitoring cycle");
            
            if (cacheRepository == null) {
                System.out.println("Cache repository not available, skipping monitoring cycle");
                return;
            }

            // Get all cached scanner keys
            var scannerKeys = cacheRepository.keys("scanner:*");
            
            for (String key : scannerKeys) {
                try {
                    // Get scanner data from cache
                    var cachedData = cacheRepository.get(key);
                    if (cachedData.isPresent()) {
                        // Parse scanner data and check status
                        // This would involve deserializing the scanner entity
                        System.out.println("Monitoring scanner: " + key);
                    }
                } catch (Exception e) {
                    System.err.println("Error monitoring scanner " + key + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error during monitoring cycle: " + e.getMessage());
        }
    }

    private boolean checkDeviceResponsiveness(DomainEntityScanner scanner) {
        try {
            // This would involve attempting to communicate with the device
            // For now, we'll simulate based on the last seen time
            DomainEntityScannerStatus status = scanner.getStatus();
            if (status == null) {
                return false;
            }

            Duration timeSinceLastSeen = status.getTimeSinceLastSeen();
            Duration maxAllowedGap = Duration.ofMinutes(5); // 5 minutes timeout
            
            return timeSinceLastSeen.compareTo(maxAllowedGap) <= 0;
            
        } catch (Exception e) {
            System.err.println("Error checking device responsiveness: " + e.getMessage());
            return false;
        }
    }

    private boolean validateStateTransition(SharedEnumConnectionState from, SharedEnumConnectionState to) {
        if (from == null || to == null) {
            return false;
        }

        // Define valid state transitions
        switch (from) {
            case CONNECTED:
                return to == SharedEnumConnectionState.DISCONNECTED || to == SharedEnumConnectionState.ERROR;
            case DISCONNECTED:
                return to == SharedEnumConnectionState.CONNECTED || to == SharedEnumConnectionState.ERROR;
            case ERROR:
                return to == SharedEnumConnectionState.CONNECTED || to == SharedEnumConnectionState.DISCONNECTED;
            default:
                return false;
        }
    }

    public boolean isMonitoring() {
        return isMonitoring;
    }

    public Duration getMonitoringInterval() {
        return monitoringInterval;
    }
}