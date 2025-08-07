package ai.shreds.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for scanner driver settings and native library management.
 * Handles TWAIN and WIA driver configuration, timeouts, and library paths.
 */
@Configuration
public class InfrastructureDriverConfiguration {

    @Value("${scanner.driver.twain-library-path:./libs/TWAIN32.dll}")
    private String twainLibraryPath;

    @Value("${scanner.driver.wia-enabled:true}")
    private Boolean wiaEnabled;

    @Value("${scanner.driver.timeout:10s}")
    private Duration driverTimeout;

    @Value("${scanner.driver.max-retry:3}")
    private Integer maxRetryAttempts;

    @Value("${scanner.driver.native-library-paths:./libs,./native-libs,/usr/local/lib}")
    private String nativeLibraryPathsString;

    @Value("${scanner.driver.twain-enabled:true}")
    private Boolean twainEnabled;

    @Value("${scanner.driver.connection-timeout:5s}")
    private Duration connectionTimeout;

    @Value("${scanner.driver.discovery-timeout:30s}")
    private Duration discoveryTimeout;

    @Value("${scanner.driver.capability-cache-ttl:5m}")
    private Duration capabilityCacheTtl;

    private List<String> nativeLibraryPaths;

    /**
     * Initializes the configuration and validates library paths.
     */
    @PostConstruct
    public void init() {
        this.nativeLibraryPaths = parseNativeLibraryPaths();
        validateLibraryPaths();
        loadSystemProperties();
    }

    /**
     * Provides TWAIN driver properties configuration.
     */
    @Bean
    public Map<String, Object> twainDriverProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        properties.put("enabled", twainEnabled);
        properties.put("libraryPath", twainLibraryPath);
        properties.put("timeout", driverTimeout.toMillis());
        properties.put("maxRetryAttempts", maxRetryAttempts);
        properties.put("connectionTimeout", connectionTimeout.toMillis());
        properties.put("discoveryTimeout", discoveryTimeout.toMillis());
        
        // TWAIN-specific settings
        properties.put("protocolMajor", 1);
        properties.put("protocolMinor", 9);
        properties.put("supportedGroups", 0x0003); // DG_CONTROL | DG_IMAGE
        properties.put("language", 13); // TWLG_ENGLISH_USA
        properties.put("country", 1);   // TWCY_USA
        
        return properties;
    }

    /**
     * Provides WIA driver properties configuration.
     */
    @Bean
    public Map<String, Object> wiaDriverProperties() {
        Map<String, Object> properties = new HashMap<>();
        
        properties.put("enabled", wiaEnabled && isWindowsPlatform());
        properties.put("timeout", driverTimeout.toMillis());
        properties.put("maxRetryAttempts", maxRetryAttempts);
        properties.put("connectionTimeout", connectionTimeout.toMillis());
        properties.put("discoveryTimeout", discoveryTimeout.toMillis());
        
        // WIA-specific settings
        properties.put("comThreadingModel", "MTA"); // Multi-threaded apartment
        properties.put("deviceInfoFlags", 0); // WIA_DEVINFO_ENUM_LOCAL
        properties.put("transferFormat", "BMP"); // Default transfer format
        
        return properties;
    }

    /**
     * Provides a dedicated executor service for driver operations.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService driverExecutor() {
        ThreadFactory threadFactory = new ThreadFactory() {
            private final AtomicInteger threadNumber = new AtomicInteger(1);
            
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "driver-executor-" + threadNumber.getAndIncrement());
                thread.setDaemon(false);
                thread.setPriority(Thread.NORM_PRIORITY);
                return thread;
            }
        };
        
        return Executors.newFixedThreadPool(2, threadFactory); // One for TWAIN, one for WIA
    }

    /**
     * Validates that the configured library paths exist and are accessible.
     */
    private void validateLibraryPaths() {
        if (nativeLibraryPaths == null || nativeLibraryPaths.isEmpty()) {
            throw new IllegalStateException("No native library paths configured");
        }
        
        // Log the configured paths for debugging
        System.out.println("Configured native library paths: " + nativeLibraryPaths);
        
        // Validate TWAIN library path if TWAIN is enabled
        if (twainEnabled && (twainLibraryPath == null || twainLibraryPath.trim().isEmpty())) {
            System.err.println("Warning: TWAIN is enabled but no library path is configured");
        }
    }

    /**
     * Loads system properties for native library access.
     */
    private void loadSystemProperties() {
        // Add native library paths to java.library.path
        String currentLibraryPath = System.getProperty("java.library.path", "");
        StringBuilder newLibraryPath = new StringBuilder(currentLibraryPath);
        
        for (String path : nativeLibraryPaths) {
            if (!currentLibraryPath.contains(path)) {
                if (newLibraryPath.length() > 0) {
                    newLibraryPath.append(System.getProperty("path.separator"));
                }
                newLibraryPath.append(path);
            }
        }
        
        System.setProperty("java.library.path", newLibraryPath.toString());
        
        // Set TWAIN-specific system properties if needed
        if (twainEnabled) {
            System.setProperty("twain.library.path", twainLibraryPath);
        }
    }

    /**
     * Parses the native library paths from the configuration string.
     */
    private List<String> parseNativeLibraryPaths() {
        if (nativeLibraryPathsString == null || nativeLibraryPathsString.trim().isEmpty()) {
            return getDefaultLibraryPaths();
        }
        
        return Arrays.asList(nativeLibraryPathsString.split(","));
    }

    /**
     * Gets default library paths based on the operating system.
     */
    private List<String> getDefaultLibraryPaths() {
        if (isWindowsPlatform()) {
            return Arrays.asList(
                "./libs",
                "./native-libs",
                "C:\\Windows\\System32",
                "C:\\Windows\\SysWOW64",
                "C:\\Windows\\twain_32"
            );
        } else if (isMacPlatform()) {
            return Arrays.asList(
                "./libs",
                "./native-libs",
                "/usr/local/lib",
                "/usr/lib",
                "/System/Library/Frameworks"
            );
        } else {
            return Arrays.asList(
                "./libs",
                "./native-libs",
                "/usr/local/lib",
                "/usr/lib",
                "/lib"
            );
        }
    }

    /**
     * Checks if the current platform is Windows.
     */
    private boolean isWindowsPlatform() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * Checks if the current platform is macOS.
     */
    private boolean isMacPlatform() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

    // Getters for configuration values
    public String getTwainLibraryPath() {
        return twainLibraryPath;
    }

    public Boolean getWiaEnabled() {
        return wiaEnabled && isWindowsPlatform();
    }

    public Boolean getTwainEnabled() {
        return twainEnabled;
    }

    public Duration getDriverTimeout() {
        return driverTimeout;
    }

    public Integer getMaxRetryAttempts() {
        return maxRetryAttempts;
    }

    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    public Duration getDiscoveryTimeout() {
        return discoveryTimeout;
    }

    public Duration getCapabilityCacheTtl() {
        return capabilityCacheTtl;
    }

    public List<String> getNativeLibraryPaths() {
        if (nativeLibraryPaths == null) {
            nativeLibraryPaths = parseNativeLibraryPaths();
        }
        return nativeLibraryPaths;
    }

    /**
     * Gets driver configuration summary for monitoring.
     */
    public DriverConfigurationSummary getConfigurationSummary() {
        return new DriverConfigurationSummary(
            twainEnabled,
            wiaEnabled && isWindowsPlatform(),
            driverTimeout,
            maxRetryAttempts,
            nativeLibraryPaths != null ? nativeLibraryPaths.size() : 0
        );
    }

    /**
     * Driver configuration summary for monitoring and diagnostics.
     */
    public static class DriverConfigurationSummary {
        private final boolean twainEnabled;
        private final boolean wiaEnabled;
        private final Duration timeout;
        private final int maxRetries;
        private final int libraryPathCount;

        public DriverConfigurationSummary(boolean twainEnabled, boolean wiaEnabled, 
                                        Duration timeout, int maxRetries, int libraryPathCount) {
            this.twainEnabled = twainEnabled;
            this.wiaEnabled = wiaEnabled;
            this.timeout = timeout;
            this.maxRetries = maxRetries;
            this.libraryPathCount = libraryPathCount;
        }

        public boolean isTwainEnabled() {
            return twainEnabled;
        }

        public boolean isWiaEnabled() {
            return wiaEnabled;
        }

        public Duration getTimeout() {
            return timeout;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public int getLibraryPathCount() {
            return libraryPathCount;
        }

        @Override
        public String toString() {
            return String.format("DriverConfig{TWAIN=%s, WIA=%s, timeout=%s, retries=%d, paths=%d}",
                               twainEnabled, wiaEnabled, timeout, maxRetries, libraryPathCount);
        }
    }
}