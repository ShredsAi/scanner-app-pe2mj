package ai.shreds.domain.ports;

import ai.shreds.shared.enums.SharedEnumProtocolType;
import java.util.List;
import java.util.Map;

/**
 * Output port for driver interface operations in the domain layer.
 * This port is implemented by the infrastructure layer to provide
 * communication with TWAIN and WIA scanner drivers.
 */
public interface DomainOutputPortDriverInterface {

    /**
     * Initialize the TWAIN driver for scanner communication.
     * @throws RuntimeException if TWAIN driver initialization fails
     */
    void initializeTwainDriver();

    /**
     * Initialize the WIA driver for scanner communication.
     * @throws RuntimeException if WIA driver initialization fails
     */
    void initializeWiaDriver();

    /**
     * Enumerate all available TWAIN scanner devices.
     * @return list of device information maps containing device properties
     * @throws RuntimeException if TWAIN enumeration fails
     */
    List<Map<String, Object>> enumerateTwainDevices();

    /**
     * Enumerate all available WIA scanner devices.
     * @return list of device information maps containing device properties
     * @throws RuntimeException if WIA enumeration fails
     */
    List<Map<String, Object>> enumerateWiaDevices();

    /**
     * Retrieve detailed capabilities for a specific scanner device.
     * @param deviceId unique identifier of the scanner device
     * @param protocol communication protocol to use (TWAIN or WIA)
     * @return map containing device capabilities and properties
     * @throws RuntimeException if capability retrieval fails
     */
    Map<String, Object> getDeviceCapabilities(String deviceId, SharedEnumProtocolType protocol);
}