package ai.shreds.infrastructure.utils;

import ai.shreds.infrastructure.exceptions.InfrastructureExceptionDriverLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for loading native driver libraries (TWAIN and WIA).
 * Handles platform detection, library path resolution, and loading state management.
 */
@Component
public class InfrastructureDriverLibraryLoader {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureDriverLibraryLoader.class);
    
    private final Map<String, Boolean> loadedLibraries = new ConcurrentHashMap<>();
    private final List<String> librarySearchPaths;
    private final String platformName;

    public InfrastructureDriverLibraryLoader() {
        this.platformName = detectPlatform();
        this.librarySearchPaths = initializeSearchPaths();
        logger.info("Initialized driver library loader for platform: {}", platformName);
    }

    /**
     * Loads the TWAIN library if not already loaded.
     */
    public void loadTwainLibrary() {
        String libraryName = getTwainLibraryName();
        if (isLibraryLoaded(libraryName)) {
            logger.debug("TWAIN library already loaded: {}", libraryName);
            return;
        }

        try {
            String libraryPath = findLibraryPath(libraryName);
            if (libraryPath != null) {
                loadNativeLibrary(libraryPath);
                loadedLibraries.put(libraryName, true);
                logger.info("Successfully loaded TWAIN library: {}", libraryPath);
            } else {
                // Try system library loading
                System.loadLibrary(getSystemLibraryName(libraryName));
                loadedLibraries.put(libraryName, true);
                logger.info("Successfully loaded TWAIN library from system path");
            }
        } catch (Exception e) {
            logger.error("Failed to load TWAIN library: {}", e.getMessage(), e);
            throw new InfrastructureExceptionDriverLoadException(
                "Failed to load TWAIN library: " + e.getMessage(),
                libraryName,
                findLibraryPath(libraryName),
                e
            );
        }
    }

    /**
     * Loads the WIA library if not already loaded (Windows only).
     */
    public void loadWiaLibrary() {
        if (!isWindowsPlatform()) {
            logger.warn("WIA library is only available on Windows platform");
            return;
        }

        String libraryName = "WIA";
        if (isLibraryLoaded(libraryName)) {
            logger.debug("WIA library already loaded");
            return;
        }

        try {
            // WIA is typically available through COM, no explicit library loading needed
            // Just mark as loaded for tracking purposes
            loadedLibraries.put(libraryName, true);
            logger.info("WIA library support enabled (COM interface)");
        } catch (Exception e) {
            logger.error("Failed to enable WIA library support: {}", e.getMessage(), e);
            throw new InfrastructureExceptionDriverLoadException(
                "Failed to enable WIA library support: " + e.getMessage(),
                libraryName,
                "COM interface",
                e
            );
        }
    }

    /**
     * Checks if a library is already loaded.
     */
    public boolean isLibraryLoaded(String libraryName) {
        return loadedLibraries.getOrDefault(libraryName, false);
    }

    /**
     * Gets the version of a loaded library.
     */
    public String getLibraryVersion(String libraryName) {
        if (!isLibraryLoaded(libraryName)) {
            return "Not loaded";
        }
        
        // For TWAIN, we could query the library version
        // For now, return a default version
        switch (libraryName.toUpperCase()) {
            case "TWAIN_32":
            case "TWAIN":
                return "2.4"; // Common TWAIN version
            case "WIA":
                return "Windows built-in";
            default:
                return "Unknown";
        }
    }

    /**
     * Detects the current platform.
     */
    private String detectPlatform() {
        String osName = System.getProperty("os.name").toLowerCase();
        String osArch = System.getProperty("os.arch").toLowerCase();
        
        if (osName.contains("windows")) {
            return osArch.contains("64") ? "windows-x64" : "windows-x86";
        } else if (osName.contains("mac")) {
            return "macos";
        } else if (osName.contains("linux")) {
            return "linux";
        } else {
            return "unknown";
        }
    }

    /**
     * Finds the full path to a library file.
     */
    private String findLibraryPath(String libraryName) {
        String fileName = getLibraryFileName(libraryName);
        
        for (String searchPath : librarySearchPaths) {
            File libraryFile = new File(searchPath, fileName);
            if (libraryFile.exists() && libraryFile.canRead()) {
                logger.debug("Found library {} at: {}", libraryName, libraryFile.getAbsolutePath());
                return libraryFile.getAbsolutePath();
            }
        }
        
        logger.debug("Library {} not found in search paths", libraryName);
        return null;
    }

    /**
     * Loads a native library from the specified path.
     */
    private void loadNativeLibrary(String path) {
        try {
            System.load(path);
            logger.debug("Loaded native library from: {}", path);
        } catch (UnsatisfiedLinkError e) {
            logger.error("Failed to load native library from {}: {}", path, e.getMessage());
            throw e;
        }
    }

    /**
     * Initializes the library search paths based on the platform.
     */
    private List<String> initializeSearchPaths() {
        List<String> paths = new ArrayList<>();
        
        // Add current working directory
        paths.add(System.getProperty("user.dir"));
        
        // Add libs subdirectory
        paths.add(new File(System.getProperty("user.dir"), "libs").getAbsolutePath());
        paths.add(new File(System.getProperty("user.dir"), "native-libs").getAbsolutePath());
        
        // Add platform-specific paths
        if (isWindowsPlatform()) {
            paths.add("C:\\Windows\\System32");
            paths.add("C:\\Windows\\SysWOW64");
            
            // Common TWAIN installation paths
            paths.add("C:\\Windows\\twain_32");
            paths.add("C:\\Program Files\\Common Files\\TWAIN");
            paths.add("C:\\Program Files (x86)\\Common Files\\TWAIN");
        } else if (isMacPlatform()) {
            paths.add("/usr/local/lib");
            paths.add("/usr/lib");
            paths.add("/System/Library/Frameworks/TWAIN.framework");
        } else if (isLinuxPlatform()) {
            paths.add("/usr/local/lib");
            paths.add("/usr/lib");
            paths.add("/lib");
        }
        
        // Add Java library path
        String javaLibraryPath = System.getProperty("java.library.path");
        if (javaLibraryPath != null) {
            String[] javaPaths = javaLibraryPath.split(File.pathSeparator);
            paths.addAll(Arrays.asList(javaPaths));
        }
        
        logger.debug("Initialized {} library search paths", paths.size());
        return paths;
    }

    /**
     * Gets the appropriate library file name for the platform.
     */
    private String getLibraryFileName(String libraryName) {
        if (isWindowsPlatform()) {
            return libraryName.toLowerCase() + ".dll";
        } else if (isMacPlatform()) {
            return "lib" + libraryName.toLowerCase() + ".dylib";
        } else {
            return "lib" + libraryName.toLowerCase() + ".so";
        }
    }

    /**
     * Gets the system library name (without file extension).
     */
    private String getSystemLibraryName(String libraryName) {
        if ("TWAIN".equalsIgnoreCase(libraryName) && isWindowsPlatform()) {
            return "TWAIN_32";
        }
        return libraryName;
    }

    /**
     * Gets the TWAIN library name for the current platform.
     */
    private String getTwainLibraryName() {
        if (isWindowsPlatform()) {
            return "TWAIN_32";
        } else if (isMacPlatform()) {
            return "TWAIN";
        } else {
            return "twain"; // Linux SANE-based TWAIN
        }
    }

    private boolean isWindowsPlatform() {
        return platformName.startsWith("windows");
    }

    private boolean isMacPlatform() {
        return platformName.equals("macos");
    }

    private boolean isLinuxPlatform() {
        return platformName.equals("linux");
    }
}