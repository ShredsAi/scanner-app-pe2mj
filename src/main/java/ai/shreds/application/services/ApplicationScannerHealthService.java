package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationInputPortScannerHealth;
import ai.shreds.application.ports.ApplicationOutputPortCacheManager;
import ai.shreds.domain.services.DomainServiceDeviceMonitoring;
import ai.shreds.shared.dtos.SharedHealthMetricsDTO;
import ai.shreds.shared.dtos.SharedDTODiscoveryCycleStatistics;
import ai.shreds.shared.dtos.SharedDTOErrorRateMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Application service for scanner health monitoring operations.
 * Implements the scanner health input port and provides health metrics.
 */
@Service
public class ApplicationScannerHealthService implements ApplicationInputPortScannerHealth {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationScannerHealthService.class);

    private final ApplicationOutputPortCacheManager cacheManager;
    private final DomainServiceDeviceMonitoring domainMonitoringService;
    private final SharedDTODiscoveryCycleStatistics discoveryMetrics;
    private final SharedDTOErrorRateMetrics errorMetrics;

    @Autowired
    public ApplicationScannerHealthService(
            ApplicationOutputPortCacheManager cacheManager,
            DomainServiceDeviceMonitoring domainMonitoringService) {
        this.cacheManager = cacheManager;
        this.domainMonitoringService = domainMonitoringService;
        this.discoveryMetrics = initializeDiscoveryMetrics();
        this.errorMetrics = initializeErrorMetrics();
    }

    @Override
    public SharedHealthMetricsDTO getHealthMetrics() {
        logger.debug("Retrieving comprehensive health metrics");
        
        try {
            return aggregateHealthData();
        } catch (Exception e) {
            logger.error("Error retrieving health metrics", e);
            return createErrorHealthMetrics();
        }
    }

    @Override
    public Integer getConnectedDeviceCount() {
        logger.debug("Retrieving connected device count");
        
        try {
            return cacheManager.getAllCachedScanners().size();
        } catch (Exception e) {
            logger.error("Error retrieving connected device count", e);
            return 0;
        }
    }

    @Override
    public Map<String, Object> getDiscoveryCycleStatistics() {
        logger.debug("Retrieving discovery cycle statistics");
        
        try {
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalCycles", discoveryMetrics.getTotalCycles());
            statistics.put("successfulCycles", discoveryMetrics.getSuccessfulCycles());
            statistics.put("failedCycles", discoveryMetrics.getFailedCycles());
            statistics.put("averageCycleDuration", discoveryMetrics.getAverageCycleDuration());
            statistics.put("lastCycleDuration", discoveryMetrics.getLastCycleDuration());
            statistics.put("lastCycleTimestamp", discoveryMetrics.getLastCycleTimestamp());
            statistics.put("devicesDiscoveredLastCycle", discoveryMetrics.getDevicesDiscoveredLastCycle());
            statistics.put("cycleFrequency", discoveryMetrics.getCycleFrequency());
            statistics.put("errorRates", calculateErrorRates());
            
            return statistics;
        } catch (Exception e) {
            logger.error("Error retrieving discovery cycle statistics", e);
            return new HashMap<>();
        }
    }

    /**
     * Calculates error rates for different types of operations.
     */
    private Map<String, Double> calculateErrorRates() {
        Map<String, Double> errorRates = new HashMap<>();
        
        try {
            errorRates.put("overall", errorMetrics.getErrorRate());
            errorRates.put("driver", calculateDriverErrorRate());
            errorRates.put("connection", calculateConnectionErrorRate());
            errorRates.put("cache", calculateCacheErrorRate());
            
        } catch (Exception e) {
            logger.error("Error calculating error rates", e);
            errorRates.put("overall", 0.0);
        }
        
        return errorRates;
    }

    /**
     * Aggregates health data from various sources.
     */
    private SharedHealthMetricsDTO aggregateHealthData() {
        SharedHealthMetricsDTO healthMetrics = new SharedHealthMetricsDTO();
        
        // Set basic metrics
        healthMetrics.setConnectedDeviceCount(getConnectedDeviceCount());
        healthMetrics.setLastDiscoveryCycleTime(discoveryMetrics.getLastCycleDuration());
        healthMetrics.setDiscoveryErrorRate(errorMetrics.getErrorRate());
        healthMetrics.setAverageDiscoveryDuration(discoveryMetrics.getAverageCycleDuration());
        healthMetrics.setTotalDiscoveryCycles(discoveryMetrics.getTotalCycles());
        healthMetrics.setLastDiscoveryTimestamp(discoveryMetrics.getLastCycleTimestamp());
        
        // Determine overall health status
        String healthStatus = determineHealthStatus();
        healthMetrics.setHealthStatus(healthStatus);
        
        return healthMetrics;
    }

    /**
     * Determines the overall health status based on various metrics.
     */
    private String determineHealthStatus() {
        try {
            double errorRate = errorMetrics.getErrorRate();
            int connectedDevices = getConnectedDeviceCount();
            long timeSinceLastCycle = System.currentTimeMillis() - 
                    (discoveryMetrics.getLastCycleTimestamp() != null ? 
                     discoveryMetrics.getLastCycleTimestamp().toEpochMilli() : 0);
            
            // Health status logic
            if (errorRate > 0.5) {
                return "CRITICAL";
            } else if (errorRate > 0.2 || timeSinceLastCycle > 300000) { // 5 minutes
                return "WARNING";
            } else if (connectedDevices == 0 && timeSinceLastCycle > 60000) { // 1 minute
                return "WARNING";
            } else {
                return "HEALTHY";
            }
        } catch (Exception e) {
            logger.error("Error determining health status", e);
            return "UNKNOWN";
        }
    }

    /**
     * Creates error health metrics when health retrieval fails.
     */
    private SharedHealthMetricsDTO createErrorHealthMetrics() {
        SharedHealthMetricsDTO errorMetrics = new SharedHealthMetricsDTO();
        errorMetrics.setConnectedDeviceCount(0);
        errorMetrics.setLastDiscoveryCycleTime(0L);
        errorMetrics.setDiscoveryErrorRate(1.0);
        errorMetrics.setAverageDiscoveryDuration(0L);
        errorMetrics.setTotalDiscoveryCycles(0L);
        errorMetrics.setHealthStatus("ERROR");
        errorMetrics.setLastDiscoveryTimestamp(Instant.now());
        return errorMetrics;
    }

    /**
     * Initializes discovery cycle statistics.
     */
    private SharedDTODiscoveryCycleStatistics initializeDiscoveryMetrics() {
        SharedDTODiscoveryCycleStatistics metrics = new SharedDTODiscoveryCycleStatistics();
        metrics.setTotalCycles(0L);
        metrics.setSuccessfulCycles(0L);
        metrics.setFailedCycles(0L);
        metrics.setAverageCycleDuration(0L);
        metrics.setLastCycleDuration(0L);
        metrics.setLastCycleTimestamp(Instant.now());
        metrics.setDevicesDiscoveredLastCycle(0);
        metrics.setCycleFrequency(30000L); // 30 seconds default
        return metrics;
    }

    /**
     * Initializes error rate metrics.
     */
    private SharedDTOErrorRateMetrics initializeErrorMetrics() {
        SharedDTOErrorRateMetrics metrics = new SharedDTOErrorRateMetrics();
        metrics.setTotalErrors(0L);
        metrics.setDriverErrors(0L);
        metrics.setConnectionErrors(0L);
        metrics.setCacheErrors(0L);
        metrics.setErrorRate(0.0);
        metrics.setErrorsByType(new HashMap<>());
        metrics.setLastErrorTimestamp(Instant.now());
        metrics.setErrorTrend("STABLE");
        return metrics;
    }

    private double calculateDriverErrorRate() {
        long totalOperations = discoveryMetrics.getTotalCycles();
        return totalOperations > 0 ? (double) errorMetrics.getDriverErrors() / totalOperations : 0.0;
    }

    private double calculateConnectionErrorRate() {
        long totalOperations = discoveryMetrics.getTotalCycles();
        return totalOperations > 0 ? (double) errorMetrics.getConnectionErrors() / totalOperations : 0.0;
    }

    private double calculateCacheErrorRate() {
        long totalOperations = discoveryMetrics.getTotalCycles();
        return totalOperations > 0 ? (double) errorMetrics.getCacheErrors() / totalOperations : 0.0;
    }
}