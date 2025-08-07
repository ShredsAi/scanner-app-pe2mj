package ai.shreds.shared.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * Data Transfer Object for error rate metrics.
 */
public class SharedDTOErrorRateMetrics {
    
    @JsonProperty("totalErrors")
    private Long totalErrors;
    
    @JsonProperty("driverErrors")
    private Long driverErrors;
    
    @JsonProperty("connectionErrors")
    private Long connectionErrors;
    
    @JsonProperty("cacheErrors")
    private Long cacheErrors;
    
    @JsonProperty("errorRate")
    private Double errorRate;
    
    @JsonProperty("errorsByType")
    private Map<String, Long> errorsByType;
    
    @JsonProperty("lastErrorTimestamp")
    private Instant lastErrorTimestamp;
    
    @JsonProperty("errorTrend")
    private String errorTrend;
    
    // Default constructor for JSON deserialization
    public SharedDTOErrorRateMetrics() {}
    
    // Constructor with all fields
    public SharedDTOErrorRateMetrics(Long totalErrors, Long driverErrors, Long connectionErrors,
                                    Long cacheErrors, Double errorRate, Map<String, Long> errorsByType,
                                    Instant lastErrorTimestamp, String errorTrend) {
        this.totalErrors = totalErrors;
        this.driverErrors = driverErrors;
        this.connectionErrors = connectionErrors;
        this.cacheErrors = cacheErrors;
        this.errorRate = errorRate;
        this.errorsByType = errorsByType;
        this.lastErrorTimestamp = lastErrorTimestamp;
        this.errorTrend = errorTrend;
    }
    
    // Getters and Setters
    public Long getTotalErrors() { return totalErrors; }
    public void setTotalErrors(Long totalErrors) { this.totalErrors = totalErrors; }
    
    public Long getDriverErrors() { return driverErrors; }
    public void setDriverErrors(Long driverErrors) { this.driverErrors = driverErrors; }
    
    public Long getConnectionErrors() { return connectionErrors; }
    public void setConnectionErrors(Long connectionErrors) { this.connectionErrors = connectionErrors; }
    
    public Long getCacheErrors() { return cacheErrors; }
    public void setCacheErrors(Long cacheErrors) { this.cacheErrors = cacheErrors; }
    
    public Double getErrorRate() { return errorRate; }
    public void setErrorRate(Double errorRate) { this.errorRate = errorRate; }
    
    public Map<String, Long> getErrorsByType() { return errorsByType; }
    public void setErrorsByType(Map<String, Long> errorsByType) { this.errorsByType = errorsByType; }
    
    public Instant getLastErrorTimestamp() { return lastErrorTimestamp; }
    public void setLastErrorTimestamp(Instant lastErrorTimestamp) { this.lastErrorTimestamp = lastErrorTimestamp; }
    
    public String getErrorTrend() { return errorTrend; }
    public void setErrorTrend(String errorTrend) { this.errorTrend = errorTrend; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedDTOErrorRateMetrics)) return false;
        SharedDTOErrorRateMetrics that = (SharedDTOErrorRateMetrics) o;
        return Objects.equals(totalErrors, that.totalErrors) &&
               Objects.equals(driverErrors, that.driverErrors) &&
               Objects.equals(connectionErrors, that.connectionErrors) &&
               Objects.equals(cacheErrors, that.cacheErrors) &&
               Objects.equals(errorRate, that.errorRate) &&
               Objects.equals(errorsByType, that.errorsByType) &&
               Objects.equals(lastErrorTimestamp, that.lastErrorTimestamp) &&
               Objects.equals(errorTrend, that.errorTrend);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(totalErrors, driverErrors, connectionErrors, cacheErrors,
                          errorRate, errorsByType, lastErrorTimestamp, errorTrend);
    }
    
    @Override
    public String toString() {
        return "SharedDTOErrorRateMetrics{" +
               "totalErrors=" + totalErrors +
               ", driverErrors=" + driverErrors +
               ", connectionErrors=" + connectionErrors +
               ", cacheErrors=" + cacheErrors +
               ", errorRate=" + errorRate +
               ", errorsByType=" + errorsByType +
               ", lastErrorTimestamp=" + lastErrorTimestamp +
               ", errorTrend='" + errorTrend + '\'' +
               '}';
    }
}