package ai.shreds.domain.services;

import ai.shreds.domain.entities.DomainEntityScanner;
import ai.shreds.domain.ports.DomainOutputPortCacheRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;

/**
 * Domain service for cache management operations.
 */
public class DomainServiceCacheManagement {
    private final DomainOutputPortCacheRepository cacheRepository;
    private final Duration cacheTTL;
    private final Duration connectionStateTTL;

    public DomainServiceCacheManagement(DomainOutputPortCacheRepository cacheRepository, 
                                       Duration cacheTtl, 
                                       Duration connectionStateTtl) {
        this.cacheRepository = Objects.requireNonNull(cacheRepository, "Cache repository cannot be null");
        this.cacheTTL = Objects.requireNonNull(cacheTtl, "Cache TTL cannot be null");
        this.connectionStateTTL = Objects.requireNonNull(connectionStateTtl, "Connection state TTL cannot be null");
    }

    public void cacheScannerInformation(String key, String value) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Cache value cannot be null");
        }

        try {
            Duration ttl = determineTTL(key);
            cacheRepository.save(key, value, ttl);
            System.out.println("Cached scanner information with key: " + key + ", TTL: " + ttl);
        } catch (Exception e) {
            System.err.println("Error caching scanner information: " + e.getMessage());
            throw new RuntimeException("Failed to cache scanner information", e);
        }
    }

    public Optional<String> retrieveCachedScanner(String key) {
        if (key == null || key.trim().isEmpty()) {
            return Optional.empty();
        }

        try {
            return cacheRepository.get(key);
        } catch (Exception e) {
            System.err.println("Error retrieving cached scanner: " + e.getMessage());
            return Optional.empty();
        }
    }

    public void updateConnectionState(String key, String stateJson) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Cache key cannot be null or empty");
        }
        if (stateJson == null) {
            throw new IllegalArgumentException("State JSON cannot be null");
        }

        try {
            String connectionStateKey = key + ":connection_state";
            cacheRepository.save(connectionStateKey, stateJson, connectionStateTTL);
            System.out.println("Updated connection state for key: " + connectionStateKey);
        } catch (Exception e) {
            System.err.println("Error updating connection state: " + e.getMessage());
            throw new RuntimeException("Failed to update connection state", e);
        }
    }

    public void evictDisconnectedDevices() {
        try {
            System.out.println("Starting eviction of disconnected devices");
            
            // Get all scanner keys
            Set<String> scannerKeys = cacheRepository.keys("scanner:*");
            int evictedCount = 0;
            
            for (String key : scannerKeys) {
                try {
                    // Check if this is a connection state key
                    if (key.endsWith(":connection_state")) {
                        continue; // Skip connection state keys, they have their own TTL
                    }
                    
                    // Check if device is still connected by looking for connection state
                    String connectionStateKey = key + ":connection_state";
                    Optional<String> connectionState = cacheRepository.get(connectionStateKey);
                    
                    if (connectionState.isEmpty()) {
                        // No connection state found, device might be disconnected
                        // Check main scanner entry age
                        if (shouldEvictScanner(key)) {
                            cacheRepository.delete(key);
                            evictedCount++;
                            System.out.println("Evicted disconnected scanner: " + key);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error checking scanner for eviction " + key + ": " + e.getMessage());
                }
            }
            
            System.out.println("Eviction completed. Evicted " + evictedCount + " disconnected devices");
            
        } catch (Exception e) {
            System.err.println("Error during device eviction: " + e.getMessage());
        }
    }

    public Map<String, String> synchronizeWithLocalCache() {
        Map<String, String> cacheSnapshot = new HashMap<>();
        
        try {
            System.out.println("Synchronizing with local cache");
            
            // Get all scanner keys
            Set<String> scannerKeys = cacheRepository.keys("scanner:*");
            
            for (String key : scannerKeys) {
                try {
                    Optional<String> value = cacheRepository.get(key);
                    if (value.isPresent()) {
                        cacheSnapshot.put(key, value.get());
                    }
                } catch (Exception e) {
                    System.err.println("Error retrieving key during synchronization " + key + ": " + e.getMessage());
                }
            }
            
            System.out.println("Cache synchronization completed. Retrieved " + cacheSnapshot.size() + " entries");
            
        } catch (Exception e) {
            System.err.println("Error during cache synchronization: " + e.getMessage());
        }
        
        return cacheSnapshot;
    }

    public void cacheScanner(DomainEntityScanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null");
        }

        try {
            String key = "scanner:" + scanner.getScannerId().toString();
            String scannerJson = serializeScannerToJson(scanner);
            
            Duration ttl = applyCacheFreshnessRules(scanner);
            cacheRepository.save(key, scannerJson, ttl);
            
            // Also cache connection state separately with shorter TTL
            if (scanner.getStatus() != null) {
                String connectionStateKey = key + ":connection_state";
                String connectionStateJson = serializeConnectionStateToJson(scanner.getStatus());
                cacheRepository.save(connectionStateKey, connectionStateJson, connectionStateTTL);
            }
            
            System.out.println("Cached scanner: " + scanner.getScannerId());
            
        } catch (Exception e) {
            System.err.println("Error caching scanner: " + e.getMessage());
            throw new RuntimeException("Failed to cache scanner", e);
        }
    }

    public void evictScanner(String scannerId) {
        if (scannerId == null || scannerId.trim().isEmpty()) {
            return;
        }

        try {
            String key = "scanner:" + scannerId;
            cacheRepository.delete(key);
            
            // Also delete connection state
            String connectionStateKey = key + ":connection_state";
            cacheRepository.delete(connectionStateKey);
            
            System.out.println("Evicted scanner from cache: " + scannerId);
            
        } catch (Exception e) {
            System.err.println("Error evicting scanner from cache: " + e.getMessage());
        }
    }

    private Duration applyCacheFreshnessRules(DomainEntityScanner scanner) {
        if (scanner == null) {
            return cacheTTL;
        }

        // Apply different TTL based on scanner state
        if (scanner.getStatus() != null) {
            switch (scanner.getStatus().getConnectionState()) {
                case CONNECTED:
                    return cacheTTL; // Normal TTL for connected devices
                case DISCONNECTED:
                    return Duration.ofMinutes(1); // Shorter TTL for disconnected devices
                case ERROR:
                    return Duration.ofSeconds(30); // Very short TTL for error states
                default:
                    return cacheTTL;
            }
        }
        
        return cacheTTL;
    }

    private boolean shouldEvictScanner(String key) {
        // This is a simplified check - in a real implementation,
        // we would check the timestamp of the cache entry
        // For now, we'll assume scanners without connection state are candidates for eviction
        return true;
    }

    private Duration determineTTL(String key) {
        if (key.contains(":connection_state")) {
            return connectionStateTTL;
        }
        return cacheTTL;
    }

    private String serializeScannerToJson(DomainEntityScanner scanner) {
        // This would use a JSON serializer to convert the scanner to JSON
        // For now, return a simple representation
        return "{\"scannerId\":\"" + scanner.getScannerId() + "\"," +
               "\"deviceName\":\"" + scanner.getDeviceName() + "\"," +
               "\"manufacturer\":\"" + scanner.getManufacturer() + "\"," +
               "\"model\":\"" + scanner.getModel() + "\"," +
               "\"isActive\":" + scanner.isActive() + "," +
               "\"updatedAt\":\"" + scanner.getUpdatedAt() + "\"}";
    }

    private String serializeConnectionStateToJson(ai.shreds.domain.entities.DomainEntityScannerStatus status) {
        // This would use a JSON serializer to convert the status to JSON
        // For now, return a simple representation
        return "{\"connectionState\":\"" + status.getConnectionState() + "\"," +
               "\"availability\":\"" + status.getAvailability() + "\"," +
               "\"lastSeen\":\"" + status.getLastSeen() + "\"," +
               "\"hasError\":" + (status.getErrorState() != null) + "}";
    }

    // Getters
    public Duration getCacheTTL() {
        return cacheTTL;
    }

    public Duration getConnectionStateTTL() {
        return connectionStateTTL;
    }
}