package ai.shreds.application.ports;

import ai.shreds.shared.dtos.SharedScannerDiscoveredEvent;
import ai.shreds.shared.dtos.SharedScannerConnectedEvent;
import ai.shreds.shared.dtos.SharedScannerDisconnectedEvent;
import ai.shreds.shared.dtos.SharedDriverErrorEvent;

/**
 * Output port for publishing scanner-related events.
 * This port is implemented by the infrastructure layer to handle event publishing.
 */
public interface ApplicationOutputPortEventPublisher {

    /**
     * Publishes a scanner discovered event when a new scanner is found.
     * @param event the scanner discovered event containing scanner details
     */
    void publishScannerDiscoveredEvent(SharedScannerDiscoveredEvent event);

    /**
     * Publishes a scanner connected event when a scanner establishes connection.
     * @param event the scanner connected event containing connection details
     */
    void publishScannerConnectedEvent(SharedScannerConnectedEvent event);

    /**
     * Publishes a scanner disconnected event when a scanner loses connection.
     * @param event the scanner disconnected event containing disconnection details
     */
    void publishScannerDisconnectedEvent(SharedScannerDisconnectedEvent event);

    /**
     * Publishes a driver error event when driver communication fails.
     * @param event the driver error event containing error details
     */
    void publishDriverErrorEvent(SharedDriverErrorEvent event);
}