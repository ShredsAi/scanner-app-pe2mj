package ai.shreds.domain.entities;

import ai.shreds.shared.value_objects.*;
import ai.shreds.shared.dtos.SharedScannerCapabilitiesDTO;
import java.util.*;

/**
 * Domain entity representing scanner capabilities.
 */
public class DomainEntityScannerCapabilities {
    private final String capabilityId;
    private final List<SharedValueResolution> supportedResolutions;
    private final List<SharedValueColorMode> colorModes;
    private final List<SharedValuePaperSize> paperSizes;
    private final boolean duplexSupport;
    private final SharedValueScanArea maxScanArea;
    private final Map<String, Object> twainFeatures;
    private final Map<String, Object> wiaFeatures;
    private final Integer maxScanWidth;
    private final Integer maxScanHeight;
    private final Integer maxResolution;
    private final Integer minResolution;

    public DomainEntityScannerCapabilities(String capabilityId, List<SharedValueResolution> supportedResolutions,
                                          List<SharedValueColorMode> colorModes, List<SharedValuePaperSize> paperSizes,
                                          boolean duplexSupport, SharedValueScanArea maxScanArea,
                                          Map<String, Object> twainFeatures, Map<String, Object> wiaFeatures,
                                          Integer maxScanWidth, Integer maxScanHeight,
                                          Integer maxResolution, Integer minResolution) {
        this.capabilityId = Objects.requireNonNull(capabilityId, "Capability ID cannot be null");
        this.supportedResolutions = supportedResolutions != null ? new ArrayList<>(supportedResolutions) : new ArrayList<>();
        this.colorModes = colorModes != null ? new ArrayList<>(colorModes) : new ArrayList<>();
        this.paperSizes = paperSizes != null ? new ArrayList<>(paperSizes) : new ArrayList<>();
        this.duplexSupport = duplexSupport;
        this.maxScanArea = maxScanArea;
        this.twainFeatures = twainFeatures != null ? new HashMap<>(twainFeatures) : new HashMap<>();
        this.wiaFeatures = wiaFeatures != null ? new HashMap<>(wiaFeatures) : new HashMap<>();
        this.maxScanWidth = maxScanWidth;
        this.maxScanHeight = maxScanHeight;
        this.maxResolution = maxResolution;
        this.minResolution = minResolution;
    }

    public boolean supportsResolution(SharedValueResolution resolution) {
        return supportedResolutions.contains(resolution);
    }

    public boolean supportsColorMode(SharedValueColorMode colorMode) {
        return colorModes.contains(colorMode);
    }

    public boolean supportsPaperSize(SharedValuePaperSize paperSize) {
        return paperSizes.contains(paperSize);
    }

    public SharedValueResolution getMaxResolution() {
        return supportedResolutions.stream()
                .max(Comparator.comparing(SharedValueResolution::getHorizontalDpi))
                .orElse(null);
    }

    public boolean hasFeature(String featureName) {
        return twainFeatures.containsKey(featureName) || wiaFeatures.containsKey(featureName);
    }

    public SharedScannerCapabilitiesDTO toDTO() {
        return SharedScannerCapabilitiesDTO.fromEntity(this);
    }

    public static DomainEntityScannerCapabilities fromDTO(SharedScannerCapabilitiesDTO dto) {
        if (dto == null) {
            return null;
        }
        return dto.toEntity();
    }

    // Getters
    public String getCapabilityId() { return capabilityId; }
    public List<SharedValueResolution> getSupportedResolutions() { return new ArrayList<>(supportedResolutions); }
    public List<SharedValueColorMode> getColorModes() { return new ArrayList<>(colorModes); }
    public List<SharedValuePaperSize> getPaperSizes() { return new ArrayList<>(paperSizes); }
    public boolean isDuplexSupport() { return duplexSupport; }
    public SharedValueScanArea getMaxScanArea() { return maxScanArea; }
    public Map<String, Object> getTwainFeatures() { return new HashMap<>(twainFeatures); }
    public Map<String, Object> getWiaFeatures() { return new HashMap<>(wiaFeatures); }
    public Integer getMaxScanWidth() { return maxScanWidth; }
    public Integer getMaxScanHeight() { return maxScanHeight; }
    public Integer getMaxResolution() { return maxResolution; }
    public Integer getMinResolution() { return minResolution; }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainEntityScannerCapabilities that = (DomainEntityScannerCapabilities) other;
        return Objects.equals(capabilityId, that.capabilityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(capabilityId);
    }

    @Override
    public String toString() {
        return "DomainEntityScannerCapabilities{" +
               "capabilityId='" + capabilityId + '\'' +
               ", supportedResolutions=" + supportedResolutions.size() +
               ", colorModes=" + colorModes.size() +
               ", paperSizes=" + paperSizes.size() +
               ", duplexSupport=" + duplexSupport +
               '}';
    }
}