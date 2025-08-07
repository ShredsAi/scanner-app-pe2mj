package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortEventPublisher;
import ai.shreds.shared.dtos.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Infrastructure adapter that implements the application event publisher port
 * by delegating to Spring's ApplicationEventPublisher.
 */
@Component
public class InfrastructureEventPublisherAdapter implements ApplicationOutputPortEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureEventPublisherAdapter.class);
    
    private final ApplicationEventPublisher applicationEventPublisher;

    public InfrastructureEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
        logger.info("Event publisher adapter initialized");
    }

    @Override
    public void publishScannerDiscoveredEvent(SharedScannerDiscoveredEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null ScannerDiscoveredEvent");
            return;
        }
        
        try {
            // Enrich the event with additional metadata if needed
            SharedScannerDiscoveredEvent enrichedEvent = enrichEvent(event);
            
            // Publish through Spring's event system
            applicationEventPublisher.publishEvent(enrichedEvent);
            
            logEventPublication(enrichedEvent);
            logger.debug("Published ScannerDiscoveredEvent for scanner: {}", 
                        event.getScanner().getScannerId().toString());
            
        } catch (Exception e) {
            logger.error("Error publishing ScannerDiscoveredEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish ScannerDiscoveredEvent", e);
        }
    }

    @Override
    public void publishScannerConnectedEvent(SharedScannerConnectedEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null ScannerConnectedEvent");
            return;
        }
        
        try {
            // Enrich the event with additional metadata if needed
            SharedScannerConnectedEvent enrichedEvent = enrichEvent(event);
            
            // Publish through Spring's event system
            applicationEventPublisher.publishEvent(enrichedEvent);
            
            logEventPublication(enrichedEvent);
            logger.debug("Published ScannerConnectedEvent for scanner: {}", event.getScannerId());
            
        } catch (Exception e) {
            logger.error("Error publishing ScannerConnectedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish ScannerConnectedEvent", e);
        }
    }

    @Override
    public void publishScannerDisconnectedEvent(SharedScannerDisconnectedEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null ScannerDisconnectedEvent");
            return;
        }
        
        try {
            // Enrich the event with additional metadata if needed
            SharedScannerDisconnectedEvent enrichedEvent = enrichEvent(event);
            
            // Publish through Spring's event system
            applicationEventPublisher.publishEvent(enrichedEvent);
            
            logEventPublication(enrichedEvent);
            logger.debug("Published ScannerDisconnectedEvent for scanner: {} with reason: {}", 
                        event.getScannerId(), event.getReason());
            
        } catch (Exception e) {
            logger.error("Error publishing ScannerDisconnectedEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish ScannerDisconnectedEvent", e);
        }
    }

    @Override
    public void publishDriverErrorEvent(SharedDriverErrorEvent event) {
        if (event == null) {
            logger.warn("Attempted to publish null DriverErrorEvent");
            return;
        }
        
        try {
            // Enrich the event with additional metadata if needed
            SharedDriverErrorEvent enrichedEvent = enrichEvent(event);
            
            // Publish through Spring's event system
            applicationEventPublisher.publishEvent(enrichedEvent);
            
            logEventPublication(enrichedEvent);
            logger.debug("Published DriverErrorEvent for scanner: {} with error: {} ({})", 
                        event.getScannerId(), event.getErrorCode(), event.getDriverType());
            
        } catch (Exception e) {
            logger.error("Error publishing DriverErrorEvent: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish DriverErrorEvent", e);
        }
    }

    /**
     * Enriches a ScannerDiscoveredEvent with additional metadata.
     */
    private SharedScannerDiscoveredEvent enrichEvent(SharedScannerDiscoveredEvent event) {
        // Ensure correlation ID is set
        if (event.getCorrelationId() == null || event.getCorrelationId().isEmpty()) {
            return new SharedScannerDiscoveredEvent(event.getScanner()) {
                @Override
                public String getCorrelationId() {
                    return UUID.randomUUID().toString();
                }
            };
        }
        return event;
    }

    /**
     * Enriches a ScannerConnectedEvent with additional metadata.
     */
    private SharedScannerConnectedEvent enrichEvent(SharedScannerConnectedEvent event) {
        // Ensure correlation ID is set
        if (event.getCorrelationId() == null || event.getCorrelationId().isEmpty()) {
            return new SharedScannerConnectedEvent(event.getScannerId(), event.getConnectionDetails()) {
                @Override
                public String getCorrelationId() {
                    return UUID.randomUUID().toString();
                }
            };
        }
        return event;
    }

    /**
     * Enriches a ScannerDisconnectedEvent with additional metadata.
     */
    private SharedScannerDisconnectedEvent enrichEvent(SharedScannerDisconnectedEvent event) {
        // Ensure correlation ID is set
        if (event.getCorrelationId() == null || event.getCorrelationId().isEmpty()) {
            return new SharedScannerDisconnectedEvent(event.getScannerId(), event.getReason()) {
                @Override
                public String getCorrelationId() {
                    return UUID.randomUUID().toString();
                }
            };
        }
        return event;
    }

    /**
     * Enriches a DriverErrorEvent with additional metadata.
     */
    private SharedDriverErrorEvent enrichEvent(SharedDriverErrorEvent event) {
        // Ensure correlation ID is set
        if (event.getCorrelationId() == null || event.getCorrelationId().isEmpty()) {
            return new SharedDriverErrorEvent(
                event.getScannerId(), 
                event.getErrorCode(), 
                event.getErrorMessage(), 
                event.getDriverType()
            ) {
                @Override
                public String getCorrelationId() {
                    return UUID.randomUUID().toString();
                }
            };
        }
        return event;
    }

    /**
     * Logs event publication for monitoring and debugging.
     */
    private void logEventPublication(Object event) {
        if (logger.isTraceEnabled()) {
            logger.trace("Event published: {} at {}", 
                        event.getClass().getSimpleName(), 
                        Instant.now());
        }
    }

    /**
     * Gets statistics about published events.
     */
    public EventPublisherStats getStats() {
        // In a real implementation, you might track event counts, timing, etc.
        return new EventPublisherStats();
    }

    /**
     * Simple statistics class for event publishing metrics.
     */
    public static class EventPublisherStats {
        private long totalEventsPublished = 0;
        private long lastEventTimestamp = 0;
        private String lastEventType = "None";

        public long getTotalEventsPublished() {
            return totalEventsPublished;
        }

        public long getLastEventTimestamp() {
            return lastEventTimestamp;
        }

        public String getLastEventType() {
            return lastEventType;
        }

        void recordEvent(String eventType) {
            this.totalEventsPublished++;
            this.lastEventTimestamp = System.currentTimeMillis();
            this.lastEventType = eventType;
        }
    }
}