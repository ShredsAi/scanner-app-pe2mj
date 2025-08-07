package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object for discovery cycle statistics.
 */
public class SharedDTODiscoveryCycleStatistics {
    
    @JsonProperty("totalCycles")
    private Long totalCycles;
    
    @JsonProperty("successfulCycles")
    private Long successfulCycles;
    
    @JsonProperty("failedCycles")
    private Long failedCycles;
    
    @JsonProperty("averageCycleDuration")
    private Long averageCycleDuration;
    
    @JsonProperty("lastCycleDuration")
    private Long lastCycleDuration;
    
    @JsonProperty("lastCycleTimestamp")
    private Instant lastCycleTimestamp;
    
    @JsonProperty("devicesDiscoveredLastCycle")
    private Integer devicesDiscoveredLastCycle;
    
    @JsonProperty("cycleFrequency")
    private Long cycleFrequency;
    
    // Default constructor for JSON deserialization
    public SharedDTODiscoveryCycleStatistics() {}
    
    // Constructor with all fields
    public SharedDTODiscoveryCycleStatistics(Long totalCycles, Long successfulCycles, 
                                            Long failedCycles, Long averageCycleDuration,
                                            Long lastCycleDuration, Instant lastCycleTimestamp,
                                            Integer devicesDiscoveredLastCycle, Long cycleFrequency) {
        this.totalCycles = totalCycles;
        this.successfulCycles = successfulCycles;
        this.failedCycles = failedCycles;
        this.averageCycleDuration = averageCycleDuration;
        this.lastCycleDuration = lastCycleDuration;
        this.lastCycleTimestamp = lastCycleTimestamp;
        this.devicesDiscoveredLastCycle = devicesDiscoveredLastCycle;
        this.cycleFrequency = cycleFrequency;
    }
    
    // Getters and Setters
    public Long getTotalCycles() { return totalCycles; }
    public void setTotalCycles(Long totalCycles) { this.totalCycles = totalCycles; }
    
    public Long getSuccessfulCycles() { return successfulCycles; }
    public void setSuccessfulCycles(Long successfulCycles) { this.successfulCycles = successfulCycles; }
    
    public Long getFailedCycles() { return failedCycles; }
    public void setFailedCycles(Long failedCycles) { this.failedCycles = failedCycles; }
    
    public Long getAverageCycleDuration() { return averageCycleDuration; }
    public void setAverageCycleDuration(Long averageCycleDuration) { this.averageCycleDuration = averageCycleDuration; }
    
    public Long getLastCycleDuration() { return lastCycleDuration; }
    public void setLastCycleDuration(Long lastCycleDuration) { this.lastCycleDuration = lastCycleDuration; }
    
    public Instant getLastCycleTimestamp() { return lastCycleTimestamp; }
    public void setLastCycleTimestamp(Instant lastCycleTimestamp) { this.lastCycleTimestamp = lastCycleTimestamp; }
    
    public Integer getDevicesDiscoveredLastCycle() { return devicesDiscoveredLastCycle; }
    public void setDevicesDiscoveredLastCycle(Integer devicesDiscoveredLastCycle) { this.devicesDiscoveredLastCycle = devicesDiscoveredLastCycle; }
    
    public Long getCycleFrequency() { return cycleFrequency; }
    public void setCycleFrequency(Long cycleFrequency) { this.cycleFrequency = cycleFrequency; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDTODiscoveryCycleStatistics)) return false;
        SharedDTODiscoveryCycleStatistics that = (SharedDTODiscoveryCycleStatistics) o;
        return Objects.equals(totalCycles, that.totalCycles) &&
               Objects.equals(successfulCycles, that.successfulCycles) &&
               Objects.equals(failedCycles, that.failedCycles) &&
               Objects.equals(averageCycleDuration, that.averageCycleDuration) &&
               Objects.equals(lastCycleDuration, that.lastCycleDuration) &&
               Objects.equals(lastCycleTimestamp, that.lastCycleTimestamp) &&
               Objects.equals(devicesDiscoveredLastCycle, that.devicesDiscoveredLastCycle) &&
               Objects.equals(cycleFrequency, that.cycleFrequency);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalCycles, successfulCycles, failedCycles, averageCycleDuration,
                          lastCycleDuration, lastCycleTimestamp, devicesDiscoveredLastCycle, cycleFrequency);
    }
    
    @Override
    public String toString() {
        return "SharedDTODiscoveryCycleStatistics{" +
               "totalCycles=" + totalCycles +
               ", successfulCycles=" + successfulCycles +
               ", failedCycles=" + failedCycles +
               ", averageCycleDuration=" + averageCycleDuration +
               ", lastCycleDuration=" + lastCycleDuration +
               ", lastCycleTimestamp=" + lastCycleTimestamp +
               ", devicesDiscoveredLastCycle=" + devicesDiscoveredLastCycle +
               ", cycleFrequency=" + cycleFrequency +
               '}';
    }
}