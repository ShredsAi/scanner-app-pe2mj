package ai.shreds.adapters.primary;

import ai.shreds.application.ports.ApplicationInputPortScannerHealth;
import ai.shreds.shared.dtos.SharedHealthMetricsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Primary adapter that provides REST endpoints for scanner health monitoring.
 * This controller exposes health metrics and monitoring information about
 * the scanner discovery system through HTTP endpoints.
 */
@RestController
@RequestMapping("/api/scanners/health")
public class AdapterScannerHealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdapterScannerHealthController.class);
    
    private final ApplicationInputPortScannerHealth scannerHealthService;
    
    @Autowired
    public AdapterScannerHealthController(ApplicationInputPortScannerHealth scannerHealthService) {
        this.scannerHealthService = scannerHealthService;
    }
    
    /**
     * Retrieves comprehensive health metrics for the scanner discovery system.
     * 
     * @return ResponseEntity containing health metrics including connected device count,
     *         discovery cycle statistics, and error rates
     */
    @GetMapping("/metrics")
    public ResponseEntity<SharedHealthMetricsDTO> getHealthMetrics() {
        try {
            logger.debug("Retrieving scanner health metrics");
            SharedHealthMetricsDTO healthMetrics = scannerHealthService.getHealthMetrics();
            logger.debug("Successfully retrieved health metrics");
            return ResponseEntity.ok(healthMetrics);
        } catch (Exception e) {
            logger.error("Error retrieving health metrics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Gets the current count of connected scanner devices.
     * 
     * @return ResponseEntity containing the number of currently connected devices
     */
    @GetMapping("/connected-count")
    public ResponseEntity<Integer> getConnectedDeviceCount() {
        try {
            logger.debug("Retrieving connected device count");
            Integer connectedCount = scannerHealthService.getConnectedDeviceCount();
            logger.debug("Successfully retrieved connected device count: {}", connectedCount);
            return ResponseEntity.ok(connectedCount);
        } catch (Exception e) {
            logger.error("Error retrieving connected device count", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves discovery cycle statistics including success rates and timing information.
     * 
     * @return ResponseEntity containing a map with discovery cycle statistics
     */
    @GetMapping("/discovery-statistics")
    public ResponseEntity<Map<String, Object>> getDiscoveryCycleStatistics() {
        try {
            logger.debug("Retrieving discovery cycle statistics");
            Map<String, Object> statistics = scannerHealthService.getDiscoveryCycleStatistics();
            logger.debug("Successfully retrieved discovery cycle statistics");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            logger.error("Error retrieving discovery cycle statistics", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Retrieves error rates for different types of operations.
     * This endpoint provides insight into the reliability of the discovery system.
     * 
     * @return ResponseEntity containing error rates by operation type
     */
    @GetMapping("/error-rates")
    public ResponseEntity<Map<String, Double>> getErrorRates() {
        try {
            logger.debug("Retrieving error rates");
            Map<String, Object> statistics = scannerHealthService.getDiscoveryCycleStatistics();
            
            // Extract error rates from statistics
            Map<String, Double> errorRates = Map.of(
                "discoveryErrorRate", extractDoubleValue(statistics, "discoveryErrorRate", 0.0),
                "connectionErrorRate", extractDoubleValue(statistics, "connectionErrorRate", 0.0),
                "driverErrorRate", extractDoubleValue(statistics, "driverErrorRate", 0.0)
            );
            
            logger.debug("Successfully retrieved error rates");
            return ResponseEntity.ok(errorRates);
        } catch (Exception e) {
            logger.error("Error retrieving error rates", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Helper method to safely extract double values from statistics map.
     * 
     * @param statistics the statistics map
     * @param key the key to extract
     * @param defaultValue the default value if key is not found or not a number
     * @return the extracted double value or default value
     */
    private Double extractDoubleValue(Map<String, Object> statistics, String key, Double defaultValue) {
        Object value = statistics.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }
}