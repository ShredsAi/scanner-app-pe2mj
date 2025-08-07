package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedHealthMetricsDTO;
import java.util.Map;

/**
 * Input port for scanner health monitoring operations.
 * Provides methods to retrieve health metrics and statistics about the scanner discovery process.
 */
public interface ApplicationInputPortScannerHealth {

    /**
     * Retrieves comprehensive health metrics for the scanner discovery system.
     * @return health metrics including connected device count, discovery cycle statistics, and error rates
     */
    SharedHealthMetricsDTO getHealthMetrics();

    /**
     * Gets the current count of connected scanner devices.
     * @return number of currently connected devices
     */
    Integer getConnectedDeviceCount();

    /**
     * Retrieves detailed statistics about discovery cycles.
     * @return map containing discovery cycle statistics such as total cycles, success rate, average duration
     */
    Map<String, Object> getDiscoveryCycleStatistics();
}