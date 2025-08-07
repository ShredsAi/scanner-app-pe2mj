package ai.shreds.shared.dtos;

import java.time.Instant;

/**
 * Data Transfer Object for health metrics of the scanner discovery system.
 * Contains information about connected devices, discovery cycles, and system health.
 */
public class SharedHealthMetricsDTO {
    
    private Integer connectedDeviceCount;
    private Long lastDiscoveryCycleTime;
    private Double discoveryErrorRate;
    private Long averageDiscoveryDuration;
    private Long totalDiscoveryCycles;
    private String healthStatus;
    private Instant lastDiscoveryTimestamp;
    
    /**
     * Default constructor.
     */
    public SharedHealthMetricsDTO() {
    }
    
    /**
     * Constructor with all fields.
     */
    public SharedHealthMetricsDTO(Integer connectedDeviceCount, Long lastDiscoveryCycleTime,
                                 Double discoveryErrorRate, Long averageDiscoveryDuration,
                                 Long totalDiscoveryCycles, String healthStatus,
                                 Instant lastDiscoveryTimestamp) {
        this.connectedDeviceCount = connectedDeviceCount;
        this.lastDiscoveryCycleTime = lastDiscoveryCycleTime;
        this.discoveryErrorRate = discoveryErrorRate;
        this.averageDiscoveryDuration = averageDiscoveryDuration;
        this.totalDiscoveryCycles = totalDiscoveryCycles;
        this.healthStatus = healthStatus;
        this.lastDiscoveryTimestamp = lastDiscoveryTimestamp;
    }
    
    public Integer getConnectedDeviceCount() {
        return connectedDeviceCount;
    }
    
    public void setConnectedDeviceCount(Integer connectedDeviceCount) {
        this.connectedDeviceCount = connectedDeviceCount;
    }
    
    public Long getLastDiscoveryCycleTime() {
        return lastDiscoveryCycleTime;
    }
    
    public void setLastDiscoveryCycleTime(Long lastDiscoveryCycleTime) {
        this.lastDiscoveryCycleTime = lastDiscoveryCycleTime;
    }
    
    public Double getDiscoveryErrorRate() {
        return discoveryErrorRate;
    }
    
    public void setDiscoveryErrorRate(Double discoveryErrorRate) {
        this.discoveryErrorRate = discoveryErrorRate;
    }
    
    public Long getAverageDiscoveryDuration() {
        return averageDiscoveryDuration;
    }
    
    public void setAverageDiscoveryDuration(Long averageDiscoveryDuration) {
        this.averageDiscoveryDuration = averageDiscoveryDuration;
    }
    
    public Long getTotalDiscoveryCycles() {
        return totalDiscoveryCycles;
    }
    
    public void setTotalDiscoveryCycles(Long totalDiscoveryCycles) {
        this.totalDiscoveryCycles = totalDiscoveryCycles;
    }
    
    public String getHealthStatus() {
        return healthStatus;
    }
    
    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }
    
    public Instant getLastDiscoveryTimestamp() {
        return lastDiscoveryTimestamp;
    }
    
    public void setLastDiscoveryTimestamp(Instant lastDiscoveryTimestamp) {
        this.lastDiscoveryTimestamp = lastDiscoveryTimestamp;
    }
    
    @Override
    public String toString() {
        return "SharedHealthMetricsDTO{" +
                "connectedDeviceCount=" + connectedDeviceCount +
                ", lastDiscoveryCycleTime=" + lastDiscoveryCycleTime +
                ", discoveryErrorRate=" + discoveryErrorRate +
                ", averageDiscoveryDuration=" + averageDiscoveryDuration +
                ", totalDiscoveryCycles=" + totalDiscoveryCycles +
                ", healthStatus='" + healthStatus + '\'' +
                ", lastDiscoveryTimestamp=" + lastDiscoveryTimestamp +
                '}';
    }
}