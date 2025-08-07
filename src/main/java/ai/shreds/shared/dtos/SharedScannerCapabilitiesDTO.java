package ai.shreds.shared.dtos;

import ai.shreds.shared.value_objects.SharedValueResolution;
import ai.shreds.shared.value_objects.SharedValueColorMode;
import ai.shreds.shared.value_objects.SharedValuePaperSize;
import ai.shreds.shared.value_objects.SharedValueScanArea;
import ai.shreds.domain.entities.DomainEntityScannerCapabilities;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Data Transfer Object representing scanner capabilities.
 */
public class SharedScannerCapabilitiesDTO {
    
    @JsonProperty("supportedResolutions")
    private List<SharedValueResolution> supportedResolutions;
    
    @JsonProperty("colorModes")
    private List<SharedValueColorMode> colorModes;
    
    @JsonProperty("paperSizes")
    private List<SharedValuePaperSize> paperSizes;
    
    @JsonProperty("duplexSupport")
    private Boolean duplexSupport;
    
    @JsonProperty("maxScanArea")
    private SharedValueScanArea maxScanArea;
    
    @JsonProperty("twainFeatures")
    private Map<String, Object> twainFeatures;
    
    @JsonProperty("wiaFeatures")
    private Map<String, Object> wiaFeatures;
    
    @JsonProperty("maxScanWidth")
    private Integer maxScanWidth;
    
    @JsonProperty("maxScanHeight")
    private Integer maxScanHeight;
    
    @JsonProperty("maxResolution")
    private Integer maxResolution;
    
    @JsonProperty("minResolution")
    private Integer minResolution;
    
    // Default constructor for JSON deserialization
    public SharedScannerCapabilitiesDTO() {}
    
    // Constructor with all fields
    public SharedScannerCapabilitiesDTO(List<SharedValueResolution> supportedResolutions,
                                       List<SharedValueColorMode> colorModes,
                                       List<SharedValuePaperSize> paperSizes,
                                       Boolean duplexSupport,
                                       SharedValueScanArea maxScanArea,
                                       Map<String, Object> twainFeatures,
                                       Map<String, Object> wiaFeatures,
                                       Integer maxScanWidth,
                                       Integer maxScanHeight,
                                       Integer maxResolution,
                                       Integer minResolution) {
        this.supportedResolutions = supportedResolutions;
        this.colorModes = colorModes;
        this.paperSizes = paperSizes;
        this.duplexSupport = duplexSupport;
        this.maxScanArea = maxScanArea;
        this.twainFeatures = twainFeatures;
        this.wiaFeatures = wiaFeatures;
        this.maxScanWidth = maxScanWidth;
        this.maxScanHeight = maxScanHeight;
        this.maxResolution = maxResolution;
        this.minResolution = minResolution;
    }
    
    /**
     * Converts this DTO to a domain entity.
     * @return DomainEntityScannerCapabilities representing this DTO
     */
    public DomainEntityScannerCapabilities toEntity() {
        // Generate a unique capability ID if not provided
        String capabilityId = UUID.randomUUID().toString();
        
        return new DomainEntityScannerCapabilities(
            capabilityId,
            supportedResolutions,
            colorModes,
            paperSizes,
            duplexSupport != null ? duplexSupport : false,
            maxScanArea,
            twainFeatures,
            wiaFeatures,
            maxScanWidth,
            maxScanHeight,
            maxResolution,
            minResolution
        );
    }
    
    /**
     * Creates a DTO from a domain entity.
     * @param capabilities the domain entity to convert
     * @return SharedScannerCapabilitiesDTO representing the domain entity
     */
    public static SharedScannerCapabilitiesDTO fromEntity(DomainEntityScannerCapabilities capabilities) {
        if (capabilities == null) {
            return null;
        }
        
        return new SharedScannerCapabilitiesDTO(
            capabilities.getSupportedResolutions(),
            capabilities.getColorModes(),
            capabilities.getPaperSizes(),
            capabilities.isDuplexSupport(),
            capabilities.getMaxScanArea(),
            capabilities.getTwainFeatures(),
            capabilities.getWiaFeatures(),
            capabilities.getMaxScanWidth(),
            capabilities.getMaxScanHeight(),
            capabilities.getMaxResolution(),
            capabilities.getMinResolution()
        );
    }
    
    // Getters and Setters
    public List<SharedValueResolution> getSupportedResolutions() { return supportedResolutions; }
    public void setSupportedResolutions(List<SharedValueResolution> supportedResolutions) { this.supportedResolutions = supportedResolutions; }
    
    public List<SharedValueColorMode> getColorModes() { return colorModes; }
    public void setColorModes(List<SharedValueColorMode> colorModes) { this.colorModes = colorModes; }
    
    public List<SharedValuePaperSize> getPaperSizes() { return paperSizes; }
    public void setPaperSizes(List<SharedValuePaperSize> paperSizes) { this.paperSizes = paperSizes; }
    
    public Boolean getDuplexSupport() { return duplexSupport; }
    public void setDuplexSupport(Boolean duplexSupport) { this.duplexSupport = duplexSupport; }
    
    public SharedValueScanArea getMaxScanArea() { return maxScanArea; }
    public void setMaxScanArea(SharedValueScanArea maxScanArea) { this.maxScanArea = maxScanArea; }
    
    public Map<String, Object> getTwainFeatures() { return twainFeatures; }
    public void setTwainFeatures(Map<String, Object> twainFeatures) { this.twainFeatures = twainFeatures; }
    
    public Map<String, Object> getWiaFeatures() { return wiaFeatures; }
    public void setWiaFeatures(Map<String, Object> wiaFeatures) { this.wiaFeatures = wiaFeatures; }
    
    public Integer getMaxScanWidth() { return maxScanWidth; }
    public void setMaxScanWidth(Integer maxScanWidth) { this.maxScanWidth = maxScanWidth; }
    
    public Integer getMaxScanHeight() { return maxScanHeight; }
    public void setMaxScanHeight(Integer maxScanHeight) { this.maxScanHeight = maxScanHeight; }
    
    public Integer getMaxResolution() { return maxResolution; }
    public void setMaxResolution(Integer maxResolution) { this.maxResolution = maxResolution; }
    
    public Integer getMinResolution() { return minResolution; }
    public void setMinResolution(Integer minResolution) { this.minResolution = minResolution; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerCapabilitiesDTO)) return false;
        SharedScannerCapabilitiesDTO that = (SharedScannerCapabilitiesDTO) o;
        return Objects.equals(supportedResolutions, that.supportedResolutions) &&
               Objects.equals(colorModes, that.colorModes) &&
               Objects.equals(paperSizes, that.paperSizes) &&
               Objects.equals(duplexSupport, that.duplexSupport) &&
               Objects.equals(maxScanArea, that.maxScanArea) &&
               Objects.equals(twainFeatures, that.twainFeatures) &&
               Objects.equals(wiaFeatures, that.wiaFeatures) &&
               Objects.equals(maxScanWidth, that.maxScanWidth) &&
               Objects.equals(maxScanHeight, that.maxScanHeight) &&
               Objects.equals(maxResolution, that.maxResolution) &&
               Objects.equals(minResolution, that.minResolution);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(supportedResolutions, colorModes, paperSizes, duplexSupport,
                          maxScanArea, twainFeatures, wiaFeatures, maxScanWidth,
                          maxScanHeight, maxResolution, minResolution);
    }
    
    @Override
    public String toString() {
        return "SharedScannerCapabilitiesDTO{" +
               "supportedResolutions=" + supportedResolutions +
               ", colorModes=" + colorModes +
               ", paperSizes=" + paperSizes +
               ", duplexSupport=" + duplexSupport +
               ", maxScanArea=" + maxScanArea +
               ", twainFeatures=" + twainFeatures +
               ", wiaFeatures=" + wiaFeatures +
               ", maxScanWidth=" + maxScanWidth +
               ", maxScanHeight=" + maxScanHeight +
               ", maxResolution=" + maxResolution +
               ", minResolution=" + minResolution +
               '}';
    }
}