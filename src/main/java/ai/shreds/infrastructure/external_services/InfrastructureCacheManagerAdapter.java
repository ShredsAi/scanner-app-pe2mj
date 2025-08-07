package ai.shreds.infrastructure.external_services;

import ai.shreds.application.ports.ApplicationOutputPortCacheManager;
import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.services.DomainServiceCacheManagement;
import ai.shreds.shared.dtos.SharedScannerDTO;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Infrastructure adapter that implements the application cache manager port
 * by delegating to the domain cache management service.
 */
@Component
public class InfrastructureCacheManagerAdapter implements ApplicationOutputPortCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureCacheManagerAdapter.class);
    
    private final DomainServiceCacheManagement domainCacheService;
    private final SharedUtilJsonSerializer jsonSerializer;

    public InfrastructureCacheManagerAdapter(DomainServiceCacheManagement domainCacheService,
                                           SharedUtilJsonSerializer jsonSerializer) {
        this.domainCacheService = domainCacheService;
        this.jsonSerializer = jsonSerializer;
        logger.info("Cache manager adapter initialized");
    }

    @Override
    public void cacheScannerInformation(SharedScannerDTO scanner) {
        if (scanner == null) {
            logger.warn("Attempted to cache null scanner DTO");
            return;
        }
        
        try {
            // Convert DTO to JSON and cache using domain service
            String cacheKey = "scanner:" + scanner.getScannerId().toString();
            String scannerJson = jsonSerializer.serialize(scanner);
            
            domainCacheService.cacheScannerInformation(cacheKey, scannerJson);
            
            logger.debug("Cached scanner information for: {}", scanner.getScannerId().toString());
            
        } catch (Exception e) {
            logger.error("Error caching scanner information for {}: {}", 
                        scanner.getScannerId().toString(), e.getMessage(), e);
            throw new RuntimeException("Failed to cache scanner information", e);
        }
    }

    @Override
    public Optional<SharedScannerDTO> retrieveCachedScanner(String scannerId) {
        if (scannerId == null || scannerId.trim().isEmpty()) {
            logger.warn("Attempted to retrieve scanner with null or empty ID");
            return Optional.empty();
        }
        
        try {
            String cacheKey = "scanner:" + scannerId;
            Optional<String> cachedJson = domainCacheService.retrieveCachedScanner(cacheKey);
            
            if (cachedJson.isPresent()) {
                // Deserialize JSON to DTO
                SharedScannerDTO scanner = jsonSerializer.deserialize(cachedJson.get(), SharedScannerDTO.class);
                logger.debug("Retrieved cached scanner: {}", scannerId);
                return Optional.of(scanner);
            } else {
                logger.debug("Scanner not found in cache: {}", scannerId);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            logger.error("Error retrieving cached scanner {}: {}", scannerId, e.getMessage(), e);
            return Optional.empty();
        }
    }

    @Override
    public void evictDisconnectedDevices() {
        try {
            logger.info("Starting eviction of disconnected devices");
            domainCacheService.evictDisconnectedDevices();
            logger.info("Completed eviction of disconnected devices");
            
        } catch (Exception e) {
            logger.error("Error during device eviction: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to evict disconnected devices", e);
        }
    }

    @Override
    public List<SharedScannerDTO> getAllCachedScanners() {
        try {
            logger.debug("Retrieving all cached scanners");
            
            // Get all cached scanner data from domain service
            var cacheSnapshot = domainCacheService.synchronizeWithLocalCache();
            
            List<SharedScannerDTO> scanners = new ArrayList<>();
            
            for (var entry : cacheSnapshot.entrySet()) {
                try {
                    // Skip connection state entries
                    if (entry.getKey().contains(":connection_state")) {
                        continue;
                    }
                    
                    // Deserialize scanner JSON to DTO
                    SharedScannerDTO scanner = jsonSerializer.deserialize(entry.getValue(), SharedScannerDTO.class);
                    scanners.add(scanner);
                    
                } catch (Exception e) {
                    logger.warn("Error deserializing cached scanner {}: {}", 
                               entry.getKey(), e.getMessage());
                }
            }
            
            logger.debug("Retrieved {} cached scanners", scanners.size());
            return scanners;
            
        } catch (Exception e) {
            logger.error("Error retrieving all cached scanners: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    /**
     * Updates the connection state for a specific scanner.
     */
    public void updateScannerConnectionState(String scannerId, String connectionState) {
        if (scannerId == null || scannerId.trim().isEmpty()) {
            logger.warn("Attempted to update connection state with null or empty scanner ID");
            return;
        }
        
        try {
            String cacheKey = "scanner:" + scannerId;
            domainCacheService.updateConnectionState(cacheKey, connectionState);
            
            logger.debug("Updated connection state for scanner: {}", scannerId);
            
        } catch (Exception e) {
            logger.error("Error updating connection state for scanner {}: {}", 
                        scannerId, e.getMessage(), e);
        }
    }

    /**
     * Evicts a specific scanner from the cache.
     */
    public void evictScanner(String scannerId) {
        if (scannerId == null || scannerId.trim().isEmpty()) {
            logger.warn("Attempted to evict scanner with null or empty ID");
            return;
        }
        
        try {
            domainCacheService.evictScanner(scannerId);
            logger.debug("Evicted scanner from cache: {}", scannerId);
            
        } catch (Exception e) {
            logger.error("Error evicting scanner {}: {}", scannerId, e.getMessage(), e);
        }
    }

    /**
     * Gets cache statistics and information.
     */
    public CacheStats getCacheStats() {
        try {
            var cacheSnapshot = domainCacheService.synchronizeWithLocalCache();
            
            long totalEntries = cacheSnapshot.size();
            long scannerEntries = cacheSnapshot.keySet().stream()
                .filter(key -> !key.contains(":connection_state"))
                .count();
            long connectionStateEntries = totalEntries - scannerEntries;
            
            return new CacheStats(totalEntries, scannerEntries, connectionStateEntries);
            
        } catch (Exception e) {
            logger.error("Error getting cache stats: {}", e.getMessage(), e);
            return new CacheStats(0, 0, 0);
        }
    }

    /**
     * Caches a scanner using the domain entity directly.
     */
    public void cacheScannerEntity(DomainEntityScanner scanner) {
        if (scanner == null) {
            logger.warn("Attempted to cache null scanner entity");
            return;
        }
        
        try {
            // Use the domain service's cacheScanner method
            domainCacheService.cacheScanner(scanner);
            logger.debug("Cached scanner entity: {}", scanner.getScannerId());
            
        } catch (Exception e) {
            logger.error("Error caching scanner entity {}: {}", 
                        scanner.getScannerId(), e.getMessage(), e);
            throw new RuntimeException("Failed to cache scanner entity", e);
        }
    }

    /**
     * Converts a SharedScannerDTO to a DomainEntityScanner.
     */
    private DomainEntityScanner convertDtoToEntity(SharedScannerDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Scanner DTO cannot be null");
        }
        
        try {
            // Use the DTO's toEntity method if available
            return dto.toEntity();
        } catch (Exception e) {
            logger.error("Error converting DTO to entity: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert scanner DTO to entity", e);
        }
    }

    /**
     * Converts a DomainEntityScanner to a SharedScannerDTO.
     */
    private SharedScannerDTO convertEntityToDto(DomainEntityScanner entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Scanner entity cannot be null");
        }
        
        try {
            // Use the entity's toDTO method if available
            return entity.toDTO();
        } catch (Exception e) {
            logger.error("Error converting entity to DTO: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to convert scanner entity to DTO", e);
        }
    }

    /**
     * Cache statistics class.
     */
    public static class CacheStats {
        private final long totalEntries;
        private final long scannerEntries;
        private final long connectionStateEntries;
        
        public CacheStats(long totalEntries, long scannerEntries, long connectionStateEntries) {
            this.totalEntries = totalEntries;
            this.scannerEntries = scannerEntries;
            this.connectionStateEntries = connectionStateEntries;
        }
        
        public long getTotalEntries() {
            return totalEntries;
        }
        
        public long getScannerEntries() {
            return scannerEntries;
        }
        
        public long getConnectionStateEntries() {
            return connectionStateEntries;
        }
        
        @Override
        public String toString() {
            return String.format("CacheStats{total=%d, scanners=%d, connectionStates=%d}", 
                               totalEntries, scannerEntries, connectionStateEntries);
        }
    }
}