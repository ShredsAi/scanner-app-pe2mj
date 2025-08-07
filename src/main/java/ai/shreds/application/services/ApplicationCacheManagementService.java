package ai.shreds.application.services;

import ai.shreds.application.ports.ApplicationOutputPortCacheManager;
import ai.shreds.domain.ports.DomainOutputPortCacheRepository;
import ai.shreds.domain.services.DomainServiceCacheManagement;
import ai.shreds.shared.dtos.SharedScannerDTO;
import ai.shreds.shared.utils.SharedUtilJsonSerializer;
import ai.shreds.application.exceptions.ApplicationExceptionCacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Application service for cache management operations.
 * Implements the cache manager output port and provides caching functionality.
 */
@Service
public class ApplicationCacheManagementService implements ApplicationOutputPortCacheManager {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationCacheManagementService.class);
    private static final String SCANNER_KEY_PREFIX = "scanner:";
    private static final String CONNECTION_KEY_PREFIX = "connection:";

    private final DomainServiceCacheManagement domainCacheService;
    private final SharedUtilJsonSerializer jsonSerializer;
    private final Map<String, SharedScannerDTO> localCache;
    
    @Value("${scanner.cache.ttl:300}")
    private long cacheTtlSeconds;
    
    @Value("${scanner.cache.connection-ttl:60}")
    private long connectionTtlSeconds;
    
    @Value("${scanner.cache.local.enabled:true}")
    private boolean localCacheEnabled;

    @Autowired
    public ApplicationCacheManagementService(
            DomainServiceCacheManagement domainCacheService,
            SharedUtilJsonSerializer jsonSerializer) {
        this.domainCacheService = domainCacheService;
        this.jsonSerializer = jsonSerializer;
        this.localCache = new ConcurrentHashMap<>();
    }

    @Override
    public void cacheScannerInformation(SharedScannerDTO scanner) {
        if (scanner == null || scanner.getScannerId() == null) {
            logger.warn("Cannot cache null scanner or scanner with null ID");
            return;
        }
        
        String cacheKey = generateScannerCacheKey(scanner.getScannerId().toString());
        logger.debug("Caching scanner information for key: {}", cacheKey);
        
        try {
            // Serialize scanner to JSON
            String scannerJson = jsonSerializer.serialize(scanner);
            
            // Cache in domain service with TTL
            Duration ttl = Duration.ofSeconds(cacheTtlSeconds);
            domainCacheService.cacheScannerInformation(cacheKey, scannerJson);
            
            // Update local cache if enabled
            if (localCacheEnabled) {
                updateLocalCache(scanner);
            }
            
            logger.debug("Successfully cached scanner: {}", scanner.getDeviceName());
            
        } catch (Exception e) {
            logger.error("Failed to cache scanner information for: {}", cacheKey, e);
            throw new ApplicationExceptionCacheException(
                    "Failed to cache scanner information", e);
        }
    }

    @Override
    public Optional<SharedScannerDTO> retrieveCachedScanner(String scannerId) {
        if (scannerId == null || scannerId.trim().isEmpty()) {
            logger.warn("Cannot retrieve scanner with null or empty ID");
            return Optional.empty();
        }
        
        String cacheKey = generateScannerCacheKey(scannerId);
        logger.debug("Retrieving cached scanner for key: {}", cacheKey);
        
        try {
            // Check local cache first if enabled
            if (localCacheEnabled && localCache.containsKey(cacheKey)) {
                SharedScannerDTO cachedScanner = localCache.get(cacheKey);
                if (isCacheEntryValid(cachedScanner)) {
                    logger.debug("Retrieved scanner from local cache: {}", cachedScanner.getDeviceName());
                    return Optional.of(cachedScanner);
                } else {
                    // Remove expired entry from local cache
                    localCache.remove(cacheKey);
                }
            }
            
            // Retrieve from domain cache service
            Optional<String> cachedJson = domainCacheService.retrieveCachedScanner(cacheKey);
            
            if (cachedJson.isPresent()) {
                SharedScannerDTO scanner = jsonSerializer.deserialize(cachedJson.get(), SharedScannerDTO.class);
                
                // Update local cache
                if (localCacheEnabled) {
                    updateLocalCache(scanner);
                }
                
                logger.debug("Retrieved scanner from domain cache: {}", scanner.getDeviceName());
                return Optional.of(scanner);
            }
            
            logger.debug("Scanner not found in cache for key: {}", cacheKey);
            return Optional.empty();
            
        } catch (Exception e) {
            logger.error("Failed to retrieve cached scanner for: {}", cacheKey, e);
            throw new ApplicationExceptionCacheException(
                    "Failed to retrieve cached scanner", e);
        }
    }

    @Override
    public void evictDisconnectedDevices() {
        logger.debug("Evicting disconnected devices from cache");
        
        try {
            // Get all cached scanners
            List<SharedScannerDTO> allScanners = getAllCachedScanners();
            
            int evictedCount = 0;
            for (SharedScannerDTO scanner : allScanners) {
                // Check if device is disconnected or offline
                if (isDeviceDisconnected(scanner)) {
                    String cacheKey = generateScannerCacheKey(scanner.getScannerId().toString());
                    
                    // Remove from domain cache
                    domainCacheService.evictDisconnectedDevices();
                    
                    // Remove from local cache
                    if (localCacheEnabled) {
                        localCache.remove(cacheKey);
                    }
                    
                    evictedCount++;
                    logger.debug("Evicted disconnected device: {}", scanner.getDeviceName());
                }
            }
            
            logger.info("Evicted {} disconnected devices from cache", evictedCount);
            
        } catch (Exception e) {
            logger.error("Failed to evict disconnected devices", e);
            throw new ApplicationExceptionCacheException(
                    "Failed to evict disconnected devices", e);
        }
    }

    @Override
    public List<SharedScannerDTO> getAllCachedScanners() {
        logger.debug("Retrieving all cached scanners");
        
        try {
            // If local cache is enabled and not empty, return from local cache
            if (localCacheEnabled && !localCache.isEmpty()) {
                List<SharedScannerDTO> validScanners = localCache.values().stream()
                        .filter(this::isCacheEntryValid)
                        .collect(Collectors.toList());
                
                if (!validScanners.isEmpty()) {
                    logger.debug("Retrieved {} scanners from local cache", validScanners.size());
                    return validScanners;
                }
            }
            
            // Synchronize with domain cache and return all scanners
            Map<String, String> allCachedData = domainCacheService.synchronizeWithLocalCache();
            
            List<SharedScannerDTO> scanners = allCachedData.entrySet().stream()
                    .filter(entry -> entry.getKey().startsWith(SCANNER_KEY_PREFIX))
                    .map(entry -> {
                        try {
                            return jsonSerializer.deserialize(entry.getValue(), SharedScannerDTO.class);
                        } catch (Exception e) {
                            logger.error("Failed to deserialize cached scanner: {}", entry.getKey(), e);
                            return null;
                        }
                    })
                    .filter(scanner -> scanner != null)
                    .collect(Collectors.toList());
            
            // Update local cache
            if (localCacheEnabled) {
                scanners.forEach(this::updateLocalCache);
            }
            
            logger.debug("Retrieved {} scanners from domain cache", scanners.size());
            return scanners;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve all cached scanners", e);
            throw new ApplicationExceptionCacheException(
                    "Failed to retrieve all cached scanners", e);
        }
    }

    /**
     * Synchronizes the local cache with the domain cache.
     */
    public void synchronizeWithLocalCache() {
        if (!localCacheEnabled) {
            return;
        }
        
        logger.debug("Synchronizing local cache with domain cache");
        
        try {
            // Get all scanners from domain cache
            List<SharedScannerDTO> domainScanners = getAllCachedScanners();
            
            // Clear and repopulate local cache
            localCache.clear();
            domainScanners.forEach(this::updateLocalCache);
            
            // Remove expired entries
            evictExpiredEntries();
            
            logger.debug("Local cache synchronized with {} entries", localCache.size());
            
        } catch (Exception e) {
            logger.error("Failed to synchronize local cache", e);
        }
    }

    /**
     * Updates the local cache with a scanner entry.
     */
    private void updateLocalCache(SharedScannerDTO scanner) {
        if (!localCacheEnabled || scanner == null) {
            return;
        }
        
        String cacheKey = generateScannerCacheKey(scanner.getScannerId().toString());
        
        // Set cache timestamp for expiration tracking
        scanner.setUpdatedAt(Instant.now());
        localCache.put(cacheKey, scanner);
    }

    /**
     * Removes expired entries from the local cache.
     */
    private void evictExpiredEntries() {
        if (!localCacheEnabled) {
            return;
        }
        
        Instant now = Instant.now();
        Duration maxAge = Duration.ofSeconds(cacheTtlSeconds);
        
        localCache.entrySet().removeIf(entry -> {
            SharedScannerDTO scanner = entry.getValue();
            if (scanner.getUpdatedAt() != null) {
                Duration age = Duration.between(scanner.getUpdatedAt(), now);
                return age.compareTo(maxAge) > 0;
            }
            return true; // Remove entries without timestamp
        });
    }

    /**
     * Checks if a cache entry is still valid (not expired).
     */
    private boolean isCacheEntryValid(SharedScannerDTO scanner) {
        if (scanner == null || scanner.getUpdatedAt() == null) {
            return false;
        }
        
        Duration age = Duration.between(scanner.getUpdatedAt(), Instant.now());
        return age.getSeconds() < cacheTtlSeconds;
    }

    /**
     * Determines if a device is considered disconnected.
     */
    private boolean isDeviceDisconnected(SharedScannerDTO scanner) {
        if (scanner == null || scanner.getStatus() == null) {
            return true;
        }
        
        // Check connection state
        if (scanner.getStatus().getConnectionState() == 
            ai.shreds.shared.enums.SharedEnumConnectionState.DISCONNECTED) {
            return true;
        }
        
        // Check if device hasn't been seen for too long
        if (scanner.getStatus().getLastSeen() != null) {
            Duration timeSinceLastSeen = Duration.between(
                    scanner.getStatus().getLastSeen(), Instant.now());
            return timeSinceLastSeen.getSeconds() > (cacheTtlSeconds * 2); // 2x cache TTL
        }
        
        return false;
    }

    /**
     * Generates a cache key for a scanner.
     */
    private String generateScannerCacheKey(String scannerId) {
        return SCANNER_KEY_PREFIX + scannerId;
    }

    /**
     * Generates a cache key for connection information.
     */
    private String generateConnectionCacheKey(String scannerId) {
        return CONNECTION_KEY_PREFIX + scannerId;
    }
}