package ai.shreds.infrastructure.external_services;

import ai.shreds.domain.ports.DomainOutputPortDriverInterface;
import ai.shreds.infrastructure.config.InfrastructureDriverConfiguration;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Adapter that implements the domain driver interface port by delegating to
 * specific TWAIN and WIA driver implementations.
 */
@Component
public class InfrastructureDriverInterfaceAdapter implements DomainOutputPortDriverInterface {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureDriverInterfaceAdapter.class);
    
    private final InfrastructureTwainDriverInterfaceImpl twainDriver;
    private final InfrastructureWiaDriverInterfaceImpl wiaDriver;
    private final InfrastructureDriverConfiguration driverConfig;
    private final List<SharedEnumProtocolType> availableProtocols;

    public InfrastructureDriverInterfaceAdapter(InfrastructureTwainDriverInterfaceImpl twainDriver,
                                               InfrastructureWiaDriverInterfaceImpl wiaDriver,
                                               InfrastructureDriverConfiguration driverConfig) {
        this.twainDriver = twainDriver;
        this.wiaDriver = wiaDriver;
        this.driverConfig = driverConfig;
        this.availableProtocols = detectAvailableProtocols();
        
        logger.info("Driver interface adapter initialized with protocols: {}", availableProtocols);
    }

    @Override
    public void initializeTwainDriver() {
        try {
            if (availableProtocols.contains(SharedEnumProtocolType.TWAIN)) {
                twainDriver.initializeTwainDriver();
                logger.info("TWAIN driver initialized successfully");
            } else {
                logger.warn("TWAIN protocol not available on this platform");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize TWAIN driver: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void initializeWiaDriver() {
        try {
            if (availableProtocols.contains(SharedEnumProtocolType.WIA)) {
                wiaDriver.initializeWiaDriver();
                logger.info("WIA driver initialized successfully");
            } else {
                logger.warn("WIA protocol not available on this platform");
            }
        } catch (Exception e) {
            logger.error("Failed to initialize WIA driver: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<Map<String, Object>> enumerateTwainDevices() {
        if (!availableProtocols.contains(SharedEnumProtocolType.TWAIN)) {
            logger.debug("TWAIN protocol not available, returning empty device list");
            return new ArrayList<>();
        }
        
        try {
            List<Map<String, Object>> devices = twainDriver.enumerateTwainDevices();
            logger.debug("Enumerated {} TWAIN devices", devices.size());
            return devices;
        } catch (Exception e) {
            logger.error("Error enumerating TWAIN devices: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<Map<String, Object>> enumerateWiaDevices() {
        if (!availableProtocols.contains(SharedEnumProtocolType.WIA)) {
            logger.debug("WIA protocol not available, returning empty device list");
            return new ArrayList<>();
        }
        
        try {
            List<Map<String, Object>> devices = wiaDriver.enumerateWiaDevices();
            logger.debug("Enumerated {} WIA devices", devices.size());
            return devices;
        } catch (Exception e) {
            logger.error("Error enumerating WIA devices: {}", e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Object> getDeviceCapabilities(String deviceId, SharedEnumProtocolType protocol) {
        if (!availableProtocols.contains(protocol)) {
            logger.warn("Protocol {} not available on this platform", protocol);
            return new HashMap<>();
        }
        
        try {
            Object driverImpl = delegateToAppropriateDriver(protocol);
            
            if (driverImpl instanceof InfrastructureTwainDriverInterfaceImpl) {
                return ((InfrastructureTwainDriverInterfaceImpl) driverImpl)
                    .getDeviceCapabilities(deviceId, protocol);
            } else if (driverImpl instanceof InfrastructureWiaDriverInterfaceImpl) {
                return ((InfrastructureWiaDriverInterfaceImpl) driverImpl)
                    .getDeviceCapabilities(deviceId, protocol);
            } else {
                logger.error("Unknown driver implementation for protocol: {}", protocol);
                return new HashMap<>();
            }
        } catch (Exception e) {
            logger.error("Error retrieving capabilities for device {} with protocol {}: {}", 
                        deviceId, protocol, e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * Detects which protocols are available on the current platform.
     */
    private List<SharedEnumProtocolType> detectAvailableProtocols() {
        List<SharedEnumProtocolType> protocols = new ArrayList<>();
        
        // TWAIN is available on Windows, macOS, and Linux (with SANE)
        protocols.add(SharedEnumProtocolType.TWAIN);
        
        // WIA is only available on Windows
        if (isWindowsPlatform()) {
            protocols.add(SharedEnumProtocolType.WIA);
        }
        
        logger.debug("Detected available protocols: {}", protocols);
        return protocols;
    }

    /**
     * Delegates to the appropriate driver implementation based on protocol.
     */
    private Object delegateToAppropriateDriver(SharedEnumProtocolType protocol) {
        switch (protocol) {
            case TWAIN:
                return twainDriver;
            case WIA:
                return wiaDriver;
            default:
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
        }
    }

    /**
     * Checks if the current platform is Windows.
     */
    private boolean isWindowsPlatform() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * Gets the list of available protocols on this platform.
     */
    public List<SharedEnumProtocolType> getAvailableProtocols() {
        return new ArrayList<>(availableProtocols);
    }

    /**
     * Checks if a specific protocol is available.
     */
    public boolean isProtocolAvailable(SharedEnumProtocolType protocol) {
        return availableProtocols.contains(protocol);
    }

    /**
     * Gets driver configuration information.
     */
    public Map<String, Object> getDriverConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("availableProtocols", availableProtocols.stream()
            .map(Enum::name)
            .collect(Collectors.toList()));
        config.put("platform", System.getProperty("os.name"));
        config.put("architecture", System.getProperty("os.arch"));
        return config;
    }
}