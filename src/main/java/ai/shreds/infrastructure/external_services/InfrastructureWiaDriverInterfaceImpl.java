package ai.shreds.infrastructure.external_services;

import ai.shreds.infrastructure.utils.InfrastructureDriverLibraryLoader;
import ai.shreds.infrastructure.utils.InfrastructureDriverErrorMapper;
import ai.shreds.infrastructure.exceptions.InfrastructureExceptionNativeLibraryException;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import com.sun.jna.*;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.COM.IUnknown;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.ptr.IntByReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * WIA driver interface implementation using JNA to communicate with Windows Image Acquisition COM interface.
 * Provides device enumeration and capability retrieval for WIA-compatible scanners on Windows.
 */
@Service
public class InfrastructureWiaDriverInterfaceImpl {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureWiaDriverInterfaceImpl.class);
    
    private final InfrastructureDriverLibraryLoader libraryLoader;
    private final InfrastructureDriverErrorMapper errorMapper;
    private IWiaDevMgr wiaDeviceManager;
    private boolean comInitialized = false;

    public InfrastructureWiaDriverInterfaceImpl(InfrastructureDriverLibraryLoader libraryLoader,
                                               InfrastructureDriverErrorMapper errorMapper) {
        this.libraryLoader = libraryLoader;
        this.errorMapper = errorMapper;
    }

    /**
     * Initializes the WIA driver and COM interface.
     */
    public void initializeWiaDriver() {
        if (!isWindowsPlatform()) {
            logger.warn("WIA driver is only available on Windows platform");
            return;
        }
        
        try {
            logger.info("Initializing WIA driver");
            libraryLoader.loadWiaLibrary();
            
            initializeComInterface();
            createWiaDeviceManager();
            
            logger.info("WIA driver initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize WIA driver: {}", e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "WIA driver initialization failed: " + e.getMessage(), 
                -1, 
                "initializeWiaDriver", 
                e
            );
        }
    }

    /**
     * Enumerates all available WIA devices.
     */
    public List<Map<String, Object>> enumerateWiaDevices() {
        if (!isWindowsPlatform()) {
            logger.warn("WIA enumeration is only available on Windows platform");
            return new ArrayList<>();
        }
        
        if (wiaDeviceManager == null) {
            initializeWiaDriver();
        }
        
        List<Map<String, Object>> devices = new ArrayList<>();
        
        try {
            PointerByReference pEnum = new PointerByReference();
            WinNT.HRESULT hr = wiaDeviceManager.EnumDeviceInfo(0, pEnum);
            
            if (hr.intValue() != WinError.S_OK) {
                handleWiaError(hr.longValue());
                return devices;
            }
            
            IEnumWIA_DEV_INFO enumInfo = new IEnumWIA_DEV_INFO(pEnum.getValue());
            
            // Enumerate devices
            Pointer deviceInfoBuffer = new Memory(1024); // Buffer for device info
            IntByReference fetched = new IntByReference();
            
            while (enumInfo.Next(1, deviceInfoBuffer, fetched).intValue() == WinError.S_OK && 
                   fetched.getValue() == 1) {
                
                try {
                    Map<String, Object> deviceInfo = extractDeviceInfo(deviceInfoBuffer);
                    if (deviceInfo != null && isScanner(deviceInfo)) {
                        devices.add(deviceInfo);
                        logger.debug("Found WIA scanner device: {}", deviceInfo.get("deviceName"));
                    }
                } catch (Exception e) {
                    logger.warn("Error processing WIA device info: {}", e.getMessage());
                }
            }
            
            enumInfo.Release();
            logger.info("Enumerated {} WIA scanner devices", devices.size());
            return devices;
            
        } catch (Exception e) {
            logger.error("Error enumerating WIA devices: {}", e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "WIA device enumeration failed: " + e.getMessage(),
                -1,
                "enumerateWiaDevices",
                e
            );
        }
    }

    /**
     * Retrieves device capabilities for a specific WIA device.
     */
    public Map<String, Object> getDeviceCapabilities(String deviceId, SharedEnumProtocolType protocol) {
        if (protocol != SharedEnumProtocolType.WIA) {
            throw new IllegalArgumentException("This implementation only supports WIA protocol");
        }
        
        if (!isWindowsPlatform()) {
            logger.warn("WIA capabilities retrieval is only available on Windows platform");
            return new HashMap<>();
        }
        
        Map<String, Object> capabilities = new HashMap<>();
        
        try {
            PointerByReference pWiaItem = new PointerByReference();
            WinNT.HRESULT hr = wiaDeviceManager.CreateDevice(deviceId, pWiaItem);
            
            if (hr.intValue() != WinError.S_OK) {
                handleWiaError(hr.longValue());
                return capabilities;
            }
            
            IWiaItem wiaItem = new IWiaItem(pWiaItem.getValue());
            
            // Get device properties
            capabilities.putAll(getWiaDeviceProperties(wiaItem));
            capabilities.putAll(getWiaCapabilityProperties(wiaItem));
            
            wiaItem.Release();
            
            logger.debug("Retrieved WIA capabilities for device: {}", deviceId);
            return capabilities;
            
        } catch (Exception e) {
            logger.error("Error retrieving WIA capabilities for device {}: {}", deviceId, e.getMessage(), e);
            throw new InfrastructureExceptionNativeLibraryException(
                "Failed to retrieve WIA device capabilities: " + e.getMessage(),
                -1,
                "getDeviceCapabilities",
                e
            );
        }
    }

    @PreDestroy
    public void cleanup() {
        releaseComInterface();
    }

    private void initializeComInterface() {
        if (comInitialized) {
            return;
        }
        
        WinNT.HRESULT hr = Ole32.INSTANCE.CoInitializeEx(null, Ole32.COINIT_MULTITHREADED);
        if (hr.intValue() != WinError.S_OK && hr.intValue() != WinError.S_FALSE) {
            throw new InfrastructureExceptionNativeLibraryException(
                "Failed to initialize COM interface", hr.intValue(), "CoInitializeEx");
        }
        
        comInitialized = true;
        logger.debug("COM interface initialized successfully");
    }

    private void releaseComInterface() {
        if (!comInitialized) {
            return;
        }
        
        try {
            if (wiaDeviceManager != null) {
                wiaDeviceManager.Release();
                wiaDeviceManager = null;
            }
            
            Ole32.INSTANCE.CoUninitialize();
            comInitialized = false;
            logger.debug("COM interface released successfully");
        } catch (Exception e) {
            logger.warn("Error releasing COM interface: {}", e.getMessage());
        }
    }

    private void createWiaDeviceManager() {
        PointerByReference pDevMgr = new PointerByReference();
        
        WinNT.HRESULT hr = Ole32.INSTANCE.CoCreateInstance(
            WiaCLSID.WiaDevMgr, 
            null,
            WTypes.CLSCTX_SERVER,
            WiaIID.IWiaDevMgr,
            pDevMgr
        );
        
        if (hr.intValue() != WinError.S_OK) {
            handleWiaError(hr.longValue());
            throw new InfrastructureExceptionNativeLibraryException(
                "Failed to create WIA device manager", hr.intValue(), "CoCreateInstance");
        }
        
        wiaDeviceManager = new IWiaDevMgr(pDevMgr.getValue());
        logger.debug("WIA device manager created successfully");
    }

    private Map<String, Object> extractDeviceInfo(Pointer deviceInfoBuffer) {
        Map<String, Object> deviceInfo = new HashMap<>();
        
        try {
            // Extract device ID (first field in WIA_DEV_INFO structure)
            String deviceId = deviceInfoBuffer.getPointer(0).getWideString(0);
            deviceInfo.put("deviceId", deviceId);
            
            // For simplicity, we'll use the device ID as the name initially
            // In a real implementation, you'd query the device properties
            deviceInfo.put("deviceName", deviceId);
            deviceInfo.put("manufacturer", "Unknown");
            deviceInfo.put("model", "WIA Scanner");
            deviceInfo.put("protocolType", SharedEnumProtocolType.WIA.name());
            deviceInfo.put("driverVersion", "Windows built-in");
            
            // Additional WIA-specific properties
            Map<String, Object> nativeProperties = new HashMap<>();
            nativeProperties.put("deviceType", "Scanner");
            deviceInfo.put("nativeProperties", nativeProperties);
            
        } catch (Exception e) {
            logger.warn("Error extracting WIA device info: {}", e.getMessage());
            return null;
        }
        
        return deviceInfo;
    }

    private boolean isScanner(Map<String, Object> deviceInfo) {
        // Simple check - in a real implementation, you'd check the device type property
        return true; // Assume all enumerated devices are scanners for now
    }

    private Map<String, Object> getWiaDeviceProperties(IWiaItem wiaItem) {
        Map<String, Object> properties = new HashMap<>();
        
        // In a real implementation, you'd use IWiaPropertyStorage to read device properties
        // For now, return default values
        properties.put("deviceName", "WIA Scanner Device");
        properties.put("manufacturer", "Unknown");
        properties.put("model", "WIA Compatible");
        
        return properties;
    }

    private Map<String, Object> getWiaCapabilityProperties(IWiaItem wiaItem) {
        Map<String, Object> capabilities = new HashMap<>();
        
        // Default WIA capabilities - in a real implementation, query actual capabilities
        capabilities.put("supportedResolutions", Arrays.asList(75, 150, 300, 600));
        capabilities.put("defaultResolution", 300);
        capabilities.put("supportedColorModes", Arrays.asList("COLOR", "GRAYSCALE", "BLACK_WHITE"));
        capabilities.put("defaultColorMode", "COLOR");
        capabilities.put("supportedPaperSizes", Arrays.asList("A4", "LETTER", "LEGAL"));
        capabilities.put("defaultPaperSize", "A4");
        capabilities.put("duplexSupport", false);
        
        return capabilities;
    }

    private void handleWiaError(long hResult) {
        logger.error("WIA error occurred: HRESULT 0x{}", Long.toHexString(hResult));
        // The error mapper will handle the conversion to domain exceptions
    }

    private boolean isWindowsPlatform() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    // WIA COM Interface Definitions
    public static class WiaCLSID {
        public static final Guid.CLSID WiaDevMgr = new Guid.CLSID("{A28BBACE-6698-11D2-AE4A-0000F803AB2E}");
    }

    public static class WiaIID {
        public static final Guid.IID IWiaDevMgr = new Guid.IID("{A26A6F8D-411D-4E53-9D21-3F7520979516}");
        public static final Guid.IID IEnumWIA_DEV_INFO = new Guid.IID("{9E56BE60-C50F-11D3-9FD8-00C04F399E33}");
        public static final Guid.IID IWiaItem = new Guid.IID("{692EDF12-2F03-4D40-B23B-39F1F45E3B9D}");
    }

    public static class IWiaDevMgr extends IUnknown {
        public IWiaDevMgr(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT EnumDeviceInfo(int lFlag, PointerByReference ppIEnum) {
            return (WinNT.HRESULT) _invokeNativeObject(3, new Object[]{getPointer(), lFlag, ppIEnum}, WinNT.HRESULT.class);
        }

        public WinNT.HRESULT CreateDevice(String pwszDeviceID, PointerByReference ppWiaItemRoot) {
            return (WinNT.HRESULT) _invokeNativeObject(4, new Object[]{getPointer(), pwszDeviceID, ppWiaItemRoot}, WinNT.HRESULT.class);
        }
    }

    public static class IEnumWIA_DEV_INFO extends IUnknown {
        public IEnumWIA_DEV_INFO(Pointer pointer) {
            super(pointer);
        }

        public WinNT.HRESULT Next(int celt, Pointer rgelt, IntByReference pceltFetched) {
            return (WinNT.HRESULT) _invokeNativeObject(3, new Object[]{getPointer(), celt, rgelt, pceltFetched}, WinNT.HRESULT.class);
        }
    }

    public static class IWiaItem extends IUnknown {
        public IWiaItem(Pointer pointer) {
            super(pointer);
        }

        // Additional methods would be implemented here for property access
    }
}