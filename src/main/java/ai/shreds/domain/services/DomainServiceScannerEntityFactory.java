package ai.shreds.domain.services;

import ai.shreds.domain.entities.*;
import ai.shreds.domain.exceptions.DomainExceptionInvalidCapabilities;
import ai.shreds.shared.value_objects.*;
import ai.shreds.shared.enums.*;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Domain service for creating scanner entities from driver data.
 */
public class DomainServiceScannerEntityFactory {

    public DomainEntityScanner createFromTwainDevice(Map<String, Object> twainData) throws UnsupportedOperationException {
        if (twainData == null || twainData.isEmpty()) {
            throw new IllegalArgumentException("TWAIN data cannot be null or empty");
        }

        try {
            // Extract basic device information
            String deviceName = getStringValue(twainData, "ProductName", "Unknown TWAIN Device");
            String manufacturer = getStringValue(twainData, "Manufacturer", "Unknown");
            String model = getStringValue(twainData, "ProductFamily", deviceName);
            String serialNumber = getStringValue(twainData, "SerialNumber", null);
            String deviceId = getStringValue(twainData, "DeviceId", generateDeviceId(manufacturer, model));
            
            // Generate scanner ID
            SharedValueScannerId scannerId = generateScannerId(Map.of(
                "deviceId", deviceId,
                "manufacturer", manufacturer,
                "model", model
            ));

            // Create capabilities from TWAIN data
            DomainEntityScannerCapabilities capabilities = createCapabilitiesFromTwainData(twainData);
            validateCapabilities(capabilities);
            applyCapabilityRules(capabilities);

            // Create initial status
            DomainEntityScannerStatus status = new DomainEntityScannerStatus(
                SharedEnumConnectionState.CONNECTED,
                SharedEnumAvailabilityState.AVAILABLE,
                Instant.now(),
                null,
                null
            );

            return new DomainEntityScanner(
                scannerId,
                deviceName,
                manufacturer,
                model,
                serialNumber,
                true,
                capabilities,
                status,
                Instant.now(),
                Instant.now()
            );

        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to create scanner from TWAIN device: " + e.getMessage(), e);
        }
    }

    public DomainEntityScanner createFromWiaDevice(Map<String, Object> wiaData) throws UnsupportedOperationException {
        if (wiaData == null || wiaData.isEmpty()) {
            throw new IllegalArgumentException("WIA data cannot be null or empty");
        }

        try {
            // Extract basic device information
            String deviceName = getStringValue(wiaData, "Name", "Unknown WIA Device");
            String manufacturer = getStringValue(wiaData, "Manufacturer", "Unknown");
            String model = getStringValue(wiaData, "Description", deviceName);
            String serialNumber = getStringValue(wiaData, "SerialNumber", null);
            String deviceId = getStringValue(wiaData, "DeviceID", generateDeviceId(manufacturer, model));
            
            // Generate scanner ID
            SharedValueScannerId scannerId = generateScannerId(Map.of(
                "deviceId", deviceId,
                "manufacturer", manufacturer,
                "model", model
            ));

            // Create capabilities from WIA data
            DomainEntityScannerCapabilities capabilities = createCapabilitiesFromWiaData(wiaData);
            validateCapabilities(capabilities);
            applyCapabilityRules(capabilities);

            // Create initial status
            DomainEntityScannerStatus status = new DomainEntityScannerStatus(
                SharedEnumConnectionState.CONNECTED,
                SharedEnumAvailabilityState.AVAILABLE,
                Instant.now(),
                null,
                null
            );

            return new DomainEntityScanner(
                scannerId,
                deviceName,
                manufacturer,
                model,
                serialNumber,
                true,
                capabilities,
                status,
                Instant.now(),
                Instant.now()
            );

        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to create scanner from WIA device: " + e.getMessage(), e);
        }
    }

    public DomainEntityScannerCapabilities mergeCapabilities(DomainEntityScannerCapabilities existing, 
                                                            DomainEntityScannerCapabilities newCapabilities) throws UnsupportedOperationException {
        if (newCapabilities == null) {
            return existing;
        }
        if (existing == null) {
            return newCapabilities;
        }

        try {
            // Merge resolutions
            Set<SharedValueResolution> mergedResolutions = new HashSet<>(existing.getSupportedResolutions());
            mergedResolutions.addAll(newCapabilities.getSupportedResolutions());

            // Merge color modes
            Set<SharedValueColorMode> mergedColorModes = new HashSet<>(existing.getColorModes());
            mergedColorModes.addAll(newCapabilities.getColorModes());

            // Merge paper sizes
            Set<SharedValuePaperSize> mergedPaperSizes = new HashSet<>(existing.getPaperSizes());
            mergedPaperSizes.addAll(newCapabilities.getPaperSizes());

            // Merge features
            Map<String, Object> mergedTwainFeatures = new HashMap<>(existing.getTwainFeatures());
            mergedTwainFeatures.putAll(newCapabilities.getTwainFeatures());

            Map<String, Object> mergedWiaFeatures = new HashMap<>(existing.getWiaFeatures());
            mergedWiaFeatures.putAll(newCapabilities.getWiaFeatures());

            return new DomainEntityScannerCapabilities(
                existing.getCapabilityId(),
                new ArrayList<>(mergedResolutions),
                new ArrayList<>(mergedColorModes),
                new ArrayList<>(mergedPaperSizes),
                existing.isDuplexSupport() || newCapabilities.isDuplexSupport(),
                newCapabilities.getMaxScanArea() != null ? newCapabilities.getMaxScanArea() : existing.getMaxScanArea(),
                mergedTwainFeatures,
                mergedWiaFeatures,
                Math.max(existing.getMaxScanWidth() != null ? existing.getMaxScanWidth() : 0,
                        newCapabilities.getMaxScanWidth() != null ? newCapabilities.getMaxScanWidth() : 0),
                Math.max(existing.getMaxScanHeight() != null ? existing.getMaxScanHeight() : 0,
                        newCapabilities.getMaxScanHeight() != null ? newCapabilities.getMaxScanHeight() : 0),
                Math.max(existing.getMaxResolution() != null ? existing.getMaxResolution() : 0,
                        newCapabilities.getMaxResolution() != null ? newCapabilities.getMaxResolution() : 0),
                Math.min(existing.getMinResolution() != null ? existing.getMinResolution() : Integer.MAX_VALUE,
                        newCapabilities.getMinResolution() != null ? newCapabilities.getMinResolution() : Integer.MAX_VALUE)
            );

        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to merge capabilities: " + e.getMessage(), e);
        }
    }

    public SharedValueScannerId generateScannerId(Map<String, String> deviceInfo) throws UnsupportedOperationException {
        if (deviceInfo == null || deviceInfo.isEmpty()) {
            throw new IllegalArgumentException("Device info cannot be null or empty");
        }

        try {
            String deviceId = deviceInfo.get("deviceId");
            if (deviceId == null || deviceId.trim().isEmpty()) {
                // Generate device ID from manufacturer and model
                String manufacturer = deviceInfo.getOrDefault("manufacturer", "Unknown");
                String model = deviceInfo.getOrDefault("model", "Unknown");
                deviceId = generateDeviceId(manufacturer, model);
            }

            // Generate instance ID (for now, use "001" as default)
            String instanceId = deviceInfo.getOrDefault("instanceId", "001");

            return new SharedValueScannerId(deviceId, instanceId);

        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to generate scanner ID: " + e.getMessage(), e);
        }
    }

    private void validateCapabilities(DomainEntityScannerCapabilities capabilities) {
        if (capabilities == null) {
            throw new DomainExceptionInvalidCapabilities("Capabilities cannot be null", "general", null);
        }

        // Validate resolutions
        if (capabilities.getSupportedResolutions().isEmpty()) {
            throw new DomainExceptionInvalidCapabilities("Scanner must support at least one resolution", "resolution", capabilities.getSupportedResolutions());
        }

        // Validate color modes
        if (capabilities.getColorModes().isEmpty()) {
            throw new DomainExceptionInvalidCapabilities("Scanner must support at least one color mode", "colorMode", capabilities.getColorModes());
        }
    }

    private void applyCapabilityRules(DomainEntityScannerCapabilities capabilities) {
        // Apply business rules to capabilities
        // For example, ensure there's always a default resolution if none is specified
        if (capabilities.getSupportedResolutions().stream().noneMatch(SharedValueResolution::isDefault)) {
            // Set the first resolution as default
            // This would require modifying the resolution, but since they're immutable,
            // we'll just log this for now
            System.out.println("No default resolution found, first resolution will be used as default");
        }
    }

    private DomainEntityScannerCapabilities createCapabilitiesFromTwainData(Map<String, Object> twainData) {
        String capabilityId = UUID.randomUUID().toString();
        
        // Extract capabilities from TWAIN data
        List<SharedValueResolution> resolutions = extractResolutions(twainData, "TWAIN");
        List<SharedValueColorMode> colorModes = extractColorModes(twainData, "TWAIN");
        List<SharedValuePaperSize> paperSizes = extractPaperSizes(twainData, "TWAIN");
        
        return new DomainEntityScannerCapabilities(
            capabilityId,
            resolutions,
            colorModes,
            paperSizes,
            getBooleanValue(twainData, "DuplexSupport", false),
            null, // maxScanArea - would be extracted from TWAIN data
            new HashMap<>(twainData), // Store all TWAIN data as features
            new HashMap<>(), // No WIA features for TWAIN device
            getIntegerValue(twainData, "MaxWidth", 8500),
            getIntegerValue(twainData, "MaxHeight", 11700),
            getIntegerValue(twainData, "MaxResolution", 1200),
            getIntegerValue(twainData, "MinResolution", 75)
        );
    }

    private DomainEntityScannerCapabilities createCapabilitiesFromWiaData(Map<String, Object> wiaData) {
        String capabilityId = UUID.randomUUID().toString();
        
        // Extract capabilities from WIA data
        List<SharedValueResolution> resolutions = extractResolutions(wiaData, "WIA");
        List<SharedValueColorMode> colorModes = extractColorModes(wiaData, "WIA");
        List<SharedValuePaperSize> paperSizes = extractPaperSizes(wiaData, "WIA");
        
        return new DomainEntityScannerCapabilities(
            capabilityId,
            resolutions,
            colorModes,
            paperSizes,
            getBooleanValue(wiaData, "DuplexSupport", false),
            null, // maxScanArea - would be extracted from WIA data
            new HashMap<>(), // No TWAIN features for WIA device
            new HashMap<>(wiaData), // Store all WIA data as features
            getIntegerValue(wiaData, "MaxWidth", 8500),
            getIntegerValue(wiaData, "MaxHeight", 11700),
            getIntegerValue(wiaData, "MaxResolution", 1200),
            getIntegerValue(wiaData, "MinResolution", 75)
        );
    }

    private List<SharedValueResolution> extractResolutions(Map<String, Object> data, String protocol) {
        List<SharedValueResolution> resolutions = new ArrayList<>();
        
        // Default resolutions if not specified
        resolutions.add(new SharedValueResolution(75, 75, false));
        resolutions.add(new SharedValueResolution(150, 150, false));
        resolutions.add(new SharedValueResolution(300, 300, true)); // Default
        resolutions.add(new SharedValueResolution(600, 600, false));
        resolutions.add(new SharedValueResolution(1200, 1200, false));
        
        return resolutions;
    }

    private List<SharedValueColorMode> extractColorModes(Map<String, Object> data, String protocol) {
        List<SharedValueColorMode> colorModes = new ArrayList<>();
        
        // Default color modes
        colorModes.add(new SharedValueColorMode(SharedEnumColorModeType.COLOR, 24, true));
        colorModes.add(new SharedValueColorMode(SharedEnumColorModeType.GRAYSCALE, 8, false));
        colorModes.add(new SharedValueColorMode(SharedEnumColorModeType.BLACK_WHITE, 1, false));
        
        return colorModes;
    }

    private List<SharedValuePaperSize> extractPaperSizes(Map<String, Object> data, String protocol) {
        List<SharedValuePaperSize> paperSizes = new ArrayList<>();
        
        // Default paper sizes
        paperSizes.add(new SharedValuePaperSize(8.5, 11.0, SharedEnumMeasurementUnit.INCHES, SharedEnumStandardPaperSize.LETTER, true));
        paperSizes.add(new SharedValuePaperSize(210, 297, SharedEnumMeasurementUnit.MILLIMETERS, SharedEnumStandardPaperSize.A4, false));
        paperSizes.add(new SharedValuePaperSize(8.5, 14.0, SharedEnumMeasurementUnit.INCHES, SharedEnumStandardPaperSize.LEGAL, false));
        
        return paperSizes;
    }

    private String generateDeviceId(String manufacturer, String model) {
        return (manufacturer + "_" + model).replaceAll("[^a-zA-Z0-9_]", "_");
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        Object value = data.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    private Integer getIntegerValue(Map<String, Object> data, String key, Integer defaultValue) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    private Boolean getBooleanValue(Map<String, Object> data, String key, Boolean defaultValue) {
        Object value = data.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }
}