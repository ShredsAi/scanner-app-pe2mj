package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Data Transfer Object representing a hardware event from the operating system.
 */
public class SharedHardwareEvent {
    
    @JsonProperty("eventType")
    private String eventType;
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    // Default constructor for JSON deserialization
    public SharedHardwareEvent() {}
    
    /**
     * Constructor with event type and device ID.
     * @param eventType the type of hardware event (e.g., "DEVICE_ARRIVAL", "DEVICE_REMOVAL")
     * @param deviceId the device identifier
     */
    public SharedHardwareEvent(String eventType, String deviceId) {
        this.eventType = eventType;
        this.deviceId = deviceId;
    }
    
    /**
     * Gets the event type.
     * @return the event type
     */
    public String getEventType() {
        return eventType;
    }
    
    /**
     * Sets the event type.
     * @param eventType the event type
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    /**
     * Gets the device ID.
     * @return the device ID
     */
    public String getDeviceId() {
        return deviceId;
    }
    
    /**
     * Sets the device ID.
     * @param deviceId the device ID
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedHardwareEvent)) return false;
        SharedHardwareEvent that = (SharedHardwareEvent) o;
        return Objects.equals(eventType, that.eventType) &&
               Objects.equals(deviceId, that.deviceId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(eventType, deviceId);
    }
    
    @Override
    public String toString() {
        return "SharedHardwareEvent{" +
               "eventType='" + eventType + '\'' +
               ", deviceId='" + deviceId + '\'' +
               '}';
    }
}