package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.utils.InfrastructureDriverLibraryLoader;
import ai.shreds.infrastructure.utils.InfrastructureDriverErrorMapper;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionNativeLibraryException;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import com.sun.jna.*;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * TWAIN driver interface implementation using JNA to communicate with native TWAIN libraries.
 * Provides device enumeration and capability retrieval for TWAIN-compatible scanners.
 */
@Service
public class InfrastructureTwainDriverInterfaceImpl {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureTwainDriverInterfaceImpl.class);
    
    private final InfrastructureDriverLibraryLoader libraryLoader;
    private final InfrastructureDriverErrorMapper errorMapper;
    private Twain32 twainLibrary;
    private TW_IDENTITY.ByReference appId;
    private boolean sessionOpen = false;
    private long sessionHandle = 0;

    public InfrastructureTwainDriverInterfaceImpl(InfrastructureDriverLibraryLoader libraryLoader,
                                                 InfrastructureDriverErrorMapper errorMapper) {
        this.libraryLoader = libraryLoader;
        this.errorMapper = errorMapper;
    }

    /**
     * Initializes the TWAIN driver and opens a session.
     */
    public void initializeTwainDriver() {
        try {
            logger.info("Initializing TWAIN driver");
            libraryLoader.loadTwainLibrary();
            twainLibrary = Twain32.INSTANCE;
            
            setupApplicationIdentity();
            openTwainSession();
            
            logger.info("TWAIN driver initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize TWAIN driver: {}", e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "TWAIN driver initialization failed: " + e.getMessage(), 
                -1, 
                "initializeTwainDriver", 
                e
            );
        }
    }

    /**
     * Enumerates all available TWAIN devices.
     */
    public List<Map<String, Object>> enumerateTwainDevices() {
        if (!sessionOpen) {
            initializeTwainDriver();
        }
        
        List<Map<String, Object>> devices = new ArrayList<>();
        TW_IDENTITY.ByReference dsId = new TW_IDENTITY.ByReference();

        try {
            // Get first source
            short rc = twainLibrary.DSM_Entry(appId, null,
                    Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                    Twain32.MSG_GETFIRST, dsId);
            
            while (rc == Twain32.TWRC_SUCCESS) {
                Map<String, Object> deviceInfo = mapTwainDeviceToMap(dsId);
                devices.add(deviceInfo);
                logger.debug("Found TWAIN device: {}", deviceInfo.get("deviceName"));

                // Get next source
                rc = twainLibrary.DSM_Entry(appId, null,
                        Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                        Twain32.MSG_GETNEXT, dsId);
            }
            
            if (rc != Twain32.TWRC_ENDOFLIST && rc != Twain32.TWRC_SUCCESS) {
                handleTwainError(rc);
            }
            
            logger.info("Enumerated {} TWAIN devices", devices.size());
            return devices;
            
        } catch (Exception e) {
            logger.error("Error enumerating TWAIN devices: {}", e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "TWAIN device enumeration failed: " + e.getMessage(),
                -1,
                "enumerateTwainDevices",
                e
            );
        }
    }

    /**
     * Retrieves device capabilities for a specific TWAIN device.
     */
    public Map<String, Object> getDeviceCapabilities(String deviceId, SharedEnumProtocolType protocol) {
        if (protocol != SharedEnumProtocolType.TWAIN) {
            throw new IllegalArgumentException("This implementation only supports TWAIN protocol");
        }
        
        Map<String, Object> capabilities = new HashMap<>();
        TW_IDENTITY.ByReference dsId = findDeviceById(deviceId);
        
        if (dsId == null) {
            throw new IllegalArgumentException("TWAIN device not found: " + deviceId);
        }
        
        try {
            // Open the data source
            short rcOpen = twainLibrary.DSM_Entry(appId, dsId,
                    Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                    Twain32.MSG_OPENDS, null);
            
            if (rcOpen != Twain32.TWRC_SUCCESS) {
                handleTwainError(rcOpen);
                return capabilities;
            }
            
            // Get various capabilities
            capabilities.putAll(getResolutionCapabilities(dsId));
            capabilities.putAll(getColorModeCapabilities(dsId));
            capabilities.putAll(getPaperSizeCapabilities(dsId));
            capabilities.put("duplexSupport", getDuplexSupport(dsId));
            
            // Close the data source
            twainLibrary.DSM_Entry(appId, dsId,
                    Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                    Twain32.MSG_CLOSEDS, null);
            
            logger.debug("Retrieved capabilities for device: {}", deviceId);
            return capabilities;
            
        } catch (Exception e) {
            logger.error("Error retrieving capabilities for device {}: {}", deviceId, e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "Failed to retrieve TWAIN device capabilities: " + e.getMessage(),
                -1,
                "getDeviceCapabilities",
                e
            );
        }
    }

    @PreDestroy
    public void cleanup() {
        closeTwainSession();
    }

    private void setupApplicationIdentity() {
        appId = new TW_IDENTITY.ByReference();
        appId.Id = 0; // DSM will assign
        appId.Version = new TW_VERSION();
        appId.Version.MajorNum = 1;
        appId.Version.MinorNum = 0;
        appId.Version.Language = 13; // TWLG_ENGLISH_USA
        appId.Version.Country = 1; // TWCY_USA
        
        String appName = "Scanner Hardware Discovery";
        System.arraycopy(appName.getBytes(), 0, appId.ProductName, 0, 
                        Math.min(appName.length(), appId.ProductName.length - 1));
        
        String manufacturer = "AI Shreds";
        System.arraycopy(manufacturer.getBytes(), 0, appId.Manufacturer, 0,
                        Math.min(manufacturer.length(), appId.Manufacturer.length - 1));
        
        appId.ProtocolMajor = 1;
        appId.ProtocolMinor = 9;
        appId.SupportedGroups = Twain32.DG_CONTROL | Twain32.DG_IMAGE;
    }

    private void openTwainSession() {
        if (sessionOpen) {
            return;
        }
        
        short rc = twainLibrary.DSM_Entry(null, appId, Twain32.DG_CONTROL,
                Twain32.DAT_PARENT, Twain32.MSG_OPENDSM, null);
        
        if (rc != Twain32.TWRC_SUCCESS) {
            handleTwainError(rc);
            throw new InfrastructureExceptionNativeLibraryException(
                "Cannot open TWAIN DSM session", rc, "openTwainSession");
        }
        
        sessionOpen = true;
        sessionHandle = System.currentTimeMillis(); // Simple session tracking
        logger.debug("TWAIN session opened successfully");
    }

    private void closeTwainSession() {
        if (!sessionOpen) {
            return;
        }
        
        try {
            twainLibrary.DSM_Entry(null, appId, Twain32.DG_CONTROL,
                    Twain32.DAT_PARENT, Twain32.MSG_CLOSEDSM, null);
            sessionOpen = false;
            sessionHandle = 0;
            logger.debug("TWAIN session closed successfully");
        } catch (Exception e) {
            logger.warn("Error closing TWAIN session: {}", e.getMessage());
        }
    }

    private Map<String, Object> mapTwainDeviceToMap(TW_IDENTITY.ByReference device) {
        Map<String, Object> deviceMap = new HashMap<>();
        
        deviceMap.put("deviceId", Native.toString(device.ProductName).trim());
        deviceMap.put("deviceName", Native.toString(device.ProductName).trim());
        deviceMap.put("manufacturer", Native.toString(device.Manufacturer).trim());
        deviceMap.put("model", Native.toString(device.ProductFamily).trim());
        deviceMap.put("protocolType", SharedEnumProtocolType.TWAIN.name());
        deviceMap.put("driverVersion", device.Version.MajorNum + "." + device.Version.MinorNum);
        
        // Additional TWAIN-specific properties
        Map<String, Object> nativeProperties = new HashMap<>();
        nativeProperties.put("twainId", device.Id);
        nativeProperties.put("protocolMajor", device.ProtocolMajor);
        nativeProperties.put("protocolMinor", device.ProtocolMinor);
        nativeProperties.put("supportedGroups", device.SupportedGroups);
        deviceMap.put("nativeProperties", nativeProperties);
        
        return deviceMap;
    }

    private TW_IDENTITY.ByReference findDeviceById(String deviceId) {
        TW_IDENTITY.ByReference dsId = new TW_IDENTITY.ByReference();
        
        short rc = twainLibrary.DSM_Entry(appId, null,
                Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                Twain32.MSG_GETFIRST, dsId);
        
        while (rc == Twain32.TWRC_SUCCESS) {
            String currentDeviceId = Native.toString(dsId.ProductName).trim();
            if (deviceId.equals(currentDeviceId)) {
                return dsId;
            }
            
            rc = twainLibrary.DSM_Entry(appId, null,
                    Twain32.DG_CONTROL, Twain32.DAT_IDENTITY,
                    Twain32.MSG_GETNEXT, dsId);
        }
        
        return null;
    }

    private Map<String, Object> getResolutionCapabilities(TW_IDENTITY.ByReference dsId) {
        Map<String, Object> resolutions = new HashMap<>();
        // Implementation would query CAP_XRESOLUTION and CAP_YRESOLUTION
        // For now, return common resolutions
        resolutions.put("supportedResolutions", Arrays.asList(75, 150, 300, 600, 1200));
        resolutions.put("defaultResolution", 300);
        return resolutions;
    }

    private Map<String, Object> getColorModeCapabilities(TW_IDENTITY.ByReference dsId) {
        Map<String, Object> colorModes = new HashMap<>();
        // Implementation would query CAP_PIXELTYPE
        colorModes.put("supportedColorModes", Arrays.asList("COLOR", "GRAYSCALE", "BLACK_WHITE"));
        colorModes.put("defaultColorMode", "COLOR");
        return colorModes;
    }

    private Map<String, Object> getPaperSizeCapabilities(TW_IDENTITY.ByReference dsId) {
        Map<String, Object> paperSizes = new HashMap<>();
        // Implementation would query CAP_SUPPORTEDSIZES
        paperSizes.put("supportedPaperSizes", Arrays.asList("A4", "LETTER", "LEGAL"));
        paperSizes.put("defaultPaperSize", "A4");
        return paperSizes;
    }

    private boolean getDuplexSupport(TW_IDENTITY.ByReference dsId) {
        // Implementation would query CAP_DUPLEXENABLED
        return false; // Default to no duplex support
    }

    private void handleTwainError(int errorCode) {
        logger.error("TWAIN error occurred: code {}", errorCode);
        // The error mapper will handle the conversion to domain exceptions
    }

    // TWAIN JNA Interface Definition
    public interface Twain32 extends Library {
        Twain32 INSTANCE = Native.load("TWAIN_32", Twain32.class);

        short DSM_Entry(TW_IDENTITY.ByReference pOrigin,
                       TW_IDENTITY.ByReference pDest,
                       int DG, short DAT, short MSG,
                       Pointer pData);

        // Data Groups
        int DG_CONTROL = 0x0001;
        int DG_IMAGE = 0x0002;

        // Data Argument Types
        short DAT_PARENT = 0x0003;
        short DAT_IDENTITY = 0x0004;
        short DAT_CAPABILITY = 0x0001;
        short DAT_STATUS = 0x0008;

        // Messages
        short MSG_OPENDSM = 0x0301;
        short MSG_CLOSEDSM = 0x0302;
        short MSG_GETFIRST = 0x0401;
        short MSG_GETNEXT = 0x0402;
        short MSG_OPENDS = 0x0303;
        short MSG_CLOSEDS = 0x0304;
        short MSG_GET = 0x0405;
        short MSG_GETDEFAULT = 0x0404;

        // Return Codes
        short TWRC_SUCCESS = 0;
        short TWRC_FAILURE = 1;
        short TWRC_ENDOFLIST = 4;
        short TWRC_CHECKSTATUS = 2;
    }

    // TWAIN Data Structures
    public static class TW_VERSION extends Structure {
        public byte MajorNum;
        public byte MinorNum;
        public byte Language;
        public byte Country;
        public byte[] Info = new byte[34];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("MajorNum", "MinorNum", "Language", "Country", "Info");
        }
    }

    public static class TW_IDENTITY extends Structure {
        public short Id;
        public TW_VERSION Version = new TW_VERSION();
        public int ProtocolMajor;
        public int ProtocolMinor;
        public int SupportedGroups;
        public byte[] Manufacturer = new byte[34];
        public byte[] ProductFamily = new byte[34];
        public byte[] ProductName = new byte[34];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("Id", "Version", "ProtocolMajor", "ProtocolMinor",
                    "SupportedGroups", "Manufacturer", "ProductFamily", "ProductName");
        }

        public static class ByReference extends TW_IDENTITY implements Structure.ByReference {}
    }
}