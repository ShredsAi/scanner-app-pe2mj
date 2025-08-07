package ai.shreds.shared.value_objects;

import java.util.Objects;

/**
 * Value object representing a unique scanner identifier.
 * Composed of device ID and instance ID to handle multiple connections of the same device.
 */
public class SharedValueScannerId {
    private final String deviceId;
    private final String instanceId;

    public SharedValueScannerId(String deviceId, String instanceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Device ID cannot be null or empty");
        }
        if (instanceId == null || instanceId.trim().isEmpty()) {
            throw new IllegalArgumentException("Instance ID cannot be null or empty");
        }
        this.deviceId = deviceId.trim();
        this.instanceId = instanceId.trim();
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    @Override
    public String toString() {
        return deviceId + "_" + instanceId;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        SharedValueScannerId that = (SharedValueScannerId) other;
        return Objects.equals(deviceId, that.deviceId) && Objects.equals(instanceId, that.instanceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId, instanceId);
    }
}