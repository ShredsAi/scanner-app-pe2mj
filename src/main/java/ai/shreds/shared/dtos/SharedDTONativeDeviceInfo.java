package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumProtocolType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object representing native device information from drivers.
 */
public class SharedDTONativeDeviceInfo {
    
    @JsonProperty("deviceId")
    private String deviceId;
    
    @JsonProperty("deviceName")
    private String deviceName;
    
    @JsonProperty("manufacturer")
    private String manufacturer;
    
    @JsonProperty("driverVersion")
    private String driverVersion;
    
    @JsonProperty("protocolType")
    private SharedEnumProtocolType protocolType;
    
    @JsonProperty("nativeProperties")
    private Map<String, Object> nativeProperties;
    
    // Default constructor
    public SharedDTONativeDeviceInfo() {
        this.nativeProperties = new HashMap<>();
    }
    
    // Constructor with basic fields
    public SharedDTONativeDeviceInfo(String deviceId, String deviceName, String manufacturer,
                                    String driverVersion, SharedEnumProtocolType protocolType) {
        this();
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.manufacturer = manufacturer;
        this.driverVersion = driverVersion;
        this.protocolType = protocolType;
    }
    
    // Constructor with all fields
    public SharedDTONativeDeviceInfo(String deviceId, String deviceName, String manufacturer,
                                    String driverVersion, SharedEnumProtocolType protocolType,
                                    Map<String, Object> nativeProperties) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.manufacturer = manufacturer;
        this.driverVersion = driverVersion;
        this.protocolType = protocolType;
        this.nativeProperties = nativeProperties != null ? nativeProperties : new HashMap<>();
    }
    
    /**
     * Converts this DTO to a Map representation.
     * @return Map containing all properties
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("deviceId", deviceId);
        map.put("deviceName", deviceName);
        map.put("manufacturer", manufacturer);
        map.put("driverVersion", driverVersion);
        map.put("protocolType", protocolType != null ? protocolType.name() : null);
        map.put("nativeProperties", nativeProperties);
        return map;
    }
    
    /**
     * Creates a DTO from a Map representation.
     * @param map the map containing properties
     * @return new SharedDTONativeDeviceInfo instance
     */
    public static SharedDTONativeDeviceInfo fromMap(Map<String, Object> map) {
        SharedDTONativeDeviceInfo dto = new SharedDTONativeDeviceInfo();
        dto.setDeviceId((String) map.get("deviceId"));
        dto.setDeviceName((String) map.get("deviceName"));
        dto.setManufacturer((String) map.get("manufacturer"));
        dto.setDriverVersion((String) map.get("driverVersion"));
        
        Object protocolObj = map.get("protocolType");
        if (protocolObj instanceof String) {
            try {
                dto.setProtocolType(SharedEnumProtocolType.valueOf((String) protocolObj));
            } catch (IllegalArgumentException e) {
                // Handle invalid protocol type gracefully
                dto.setProtocolType(null);
            }
        }
        
        Object nativePropsObj = map.get("nativeProperties");
        if (nativePropsObj instanceof Map) {
            dto.setNativeProperties((Map<String, Object>) nativePropsObj);
        }
        
        return dto;
    }
    
    // Getters and Setters
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
    
    public String getDriverVersion() { return driverVersion; }
    public void setDriverVersion(String driverVersion) { this.driverVersion = driverVersion; }
    
    public SharedEnumProtocolType getProtocolType() { return protocolType; }
    public void setProtocolType(SharedEnumProtocolType protocolType) { this.protocolType = protocolType; }
    
    public Map<String, Object> getNativeProperties() { return nativeProperties; }
    public void setNativeProperties(Map<String, Object> nativeProperties) { this.nativeProperties = nativeProperties; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDTONativeDeviceInfo)) return false;
        SharedDTONativeDeviceInfo that = (SharedDTONativeDeviceInfo) o;
        return Objects.equals(deviceId, that.deviceId) &&
               Objects.equals(deviceName, that.deviceName) &&
               Objects.equals(manufacturer, that.manufacturer) &&
               Objects.equals(driverVersion, that.driverVersion) &&
               protocolType == that.protocolType &&
               Objects.equals(nativeProperties, that.nativeProperties);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(deviceId, deviceName, manufacturer, driverVersion, protocolType, nativeProperties);
    }
    
    @Override
    public String toString() {
        return "SharedDTONativeDeviceInfo{" +
               "deviceId='" + deviceId + '\'' +
               ", deviceName='" + deviceName + '\'' +
               ", manufacturer='" + manufacturer + '\'' +
               ", driverVersion='" + driverVersion + '\'' +
               ", protocolType=" + protocolType +
               ", nativeProperties=" + nativeProperties +
               '}';
    }
}