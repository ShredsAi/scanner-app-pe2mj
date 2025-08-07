package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScannerDTO;
import java.util.List;
import java.util.Optional;

/**
 * Output port for cache management operations.
 * This port is implemented by the infrastructure layer to handle caching operations.
 */
public interface ApplicationOutputPortCacheManager {

    /**
     * Caches scanner information with appropriate TTL settings.
     * @param scanner the scanner DTO to cache
     */
    void cacheScannerInformation(SharedScannerDTO scanner);

    /**
     * Retrieves cached scanner information by scanner ID.
     * @param scannerId the unique identifier of the scanner
     * @return optional containing the cached scanner DTO if found
     */
    Optional<SharedScannerDTO> retrieveCachedScanner(String scannerId);

    /**
     * Removes cache entries for devices that have been disconnected.
     * This method cleans up stale cache entries to prevent memory leaks.
     */
    void evictDisconnectedDevices();

    /**
     * Retrieves all cached scanner information.
     * @return list of all cached scanner DTOs
     */
    List<SharedScannerDTO> getAllCachedScanners();
}