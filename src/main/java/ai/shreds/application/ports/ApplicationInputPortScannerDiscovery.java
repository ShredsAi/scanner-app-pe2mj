package ai.shreds.application.ports;

import java.util.List;
import ai.shreds.shared.dtos.SharedScannerDTO;

public interface ApplicationInputPortScannerDiscovery {

    /**
     * Initiates the scanner discovery process.
     * @return list of discovered scanners
     */
    List<SharedScannerDTO> discoverScanners();

    /**
     * Polls all connected devices to check their current status.
     */
    void pollConnectedDevices();

    /**
     * Handles an operating system hardware event.
     * @param eventType type of hardware event (e.g., insertion, removal)
     * @param deviceId identifier of the device in the event
     */
    void handleHardwareEvent(String eventType, String deviceId);

    /**
     * Synchronizes the in-memory cache with the persistent cache.
     */
    void synchronizeCachedDevices();
}
