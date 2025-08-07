package ai.shreds.infrastructure.utils;

import ai.shreds.domain.exceptions.DomainExceptionDriverError;
import ai.shreds.domain.exceptions.DomainExceptionConnectionError;
import ai.shreds.shared.enums.SharedEnumProtocolType;
import ai.shreds.shared.enums.SharedEnumDriverType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for mapping native driver errors to domain exceptions.
 * Handles error code translation and localized message generation.
 */
@Component
public class InfrastructureDriverErrorMapper {

    private static final Logger logger = LoggerFactory.getLogger(InfrastructureDriverErrorMapper.class);
    
    private final Map<Integer, String> twainErrorCodeMappings;
    private final Map<Long, String> wiaErrorCodeMappings;
    private final Map<String, String> errorMessageTemplates;

    public InfrastructureDriverErrorMapper() {
        this.twainErrorCodeMappings = initializeTwainErrorMappings();
        this.wiaErrorCodeMappings = initializeWiaErrorMappings();
        this.errorMessageTemplates = initializeMessageTemplates();
    }

    /**
     * Maps a TWAIN error code to a domain exception.
     */
    public DomainExceptionDriverError mapTwainError(int errorCode) {
        String errorMessage = twainErrorCodeMappings.getOrDefault(errorCode, "Unknown TWAIN error");
        String localizedMessage = getLocalizedMessage("TWAIN_" + errorCode);
        String severity = determineErrorSeverity(errorCode);
        
        logger.debug("Mapping TWAIN error code {} to: {}", errorCode, errorMessage);
        
        return new DomainExceptionDriverError(
            localizedMessage != null ? localizedMessage : errorMessage,
            SharedEnumDriverType.TWAIN,
            String.valueOf(errorCode)
        );
    }

    /**
     * Maps a WIA HRESULT to a domain exception.
     */
    public DomainExceptionDriverError mapWiaError(long hResult) {
        String errorMessage = wiaErrorCodeMappings.getOrDefault(hResult, "Unknown WIA error");
        String localizedMessage = getLocalizedMessage("WIA_" + Long.toHexString(hResult).toUpperCase());
        
        logger.debug("Mapping WIA HRESULT 0x{} to: {}", Long.toHexString(hResult), errorMessage);
        
        return new DomainExceptionDriverError(
            localizedMessage != null ? localizedMessage : errorMessage,
            SharedEnumDriverType.WIA,
            "0x" + Long.toHexString(hResult).toUpperCase()
        );
    }

    /**
     * Maps a connection error to a domain exception.
     */
    public DomainExceptionConnectionError mapConnectionError(Exception error, SharedEnumProtocolType protocol) {
        String connectionId = "unknown";
        String errorMessage = "Connection failed: " + error.getMessage();
        
        if (error.getCause() != null) {
            errorMessage += " (Cause: " + error.getCause().getMessage() + ")";
        }
        
        logger.debug("Mapping connection error for protocol {}: {}", protocol, errorMessage);
        
        return new DomainExceptionConnectionError(
            errorMessage,
            connectionId,
            protocol,
            error
        );
    }

    /**
     * Gets a localized error message for the given error code.
     */
    private String getLocalizedMessage(String errorCode) {
        return errorMessageTemplates.get(errorCode);
    }

    /**
     * Determines the severity of a TWAIN error based on the error code.
     */
    private String determineErrorSeverity(int errorCode) {
        switch (errorCode) {
            case 0: // TWRC_SUCCESS
                return "INFO";
            case 1: // TWRC_FAILURE
            case 3: // TWRC_CANCEL
                return "ERROR";
            case 2: // TWRC_CHECKSTATUS
            case 4: // TWRC_ENDOFLIST
                return "WARNING";
            case 5: // TWRC_INFONOTSUPPORTED
            case 6: // TWRC_DATANOTAVAILABLE
                return "INFO";
            default:
                return "ERROR";
        }
    }

    /**
     * Initializes TWAIN error code mappings.
     */
    private Map<Integer, String> initializeTwainErrorMappings() {
        Map<Integer, String> mappings = new HashMap<>();
        
        // TWAIN Return Codes
        mappings.put(0, "Success");
        mappings.put(1, "Failure");
        mappings.put(2, "Check status");
        mappings.put(3, "Cancel");
        mappings.put(4, "End of list");
        mappings.put(5, "Info not supported");
        mappings.put(6, "Data not available");
        mappings.put(7, "Busy");
        mappings.put(8, "Scanner locked");
        
        // TWAIN Condition Codes (TWCC_*)
        mappings.put(0x8001, "Bad capability");
        mappings.put(0x8002, "Bad protocol");
        mappings.put(0x8003, "Bad value");
        mappings.put(0x8004, "Sequence error");
        mappings.put(0x8005, "Bad destination");
        mappings.put(0x8006, "Capability not supported");
        mappings.put(0x8007, "Capability bad operation");
        mappings.put(0x8008, "Capability sequence error");
        mappings.put(0x8009, "Denied");
        mappings.put(0x800A, "File exists");
        mappings.put(0x800B, "File not found");
        mappings.put(0x800C, "Not empty");
        mappings.put(0x800D, "Paper jam");
        mappings.put(0x800E, "Paper double feed");
        mappings.put(0x800F, "File write error");
        mappings.put(0x8010, "Check device online");
        mappings.put(0x8011, "Interlock open");
        mappings.put(0x8012, "Driver error");
        mappings.put(0x8013, "Device offline");
        mappings.put(0x8014, "Null container");
        mappings.put(0x8015, "Null handle");
        mappings.put(0x8016, "Bad handle");
        mappings.put(0x8017, "Bad pointer");
        mappings.put(0x8018, "Bad value");
        mappings.put(0x8019, "Low memory");
        mappings.put(0x801A, "No data source");
        
        return mappings;
    }

    /**
     * Initializes WIA error code mappings.
     */
    private Map<Long, String> initializeWiaErrorMappings() {
        Map<Long, String> mappings = new HashMap<>();
        
        // Common WIA HRESULT codes
        mappings.put(0x00000000L, "Success");
        mappings.put(0x80004001L, "Not implemented");
        mappings.put(0x80004002L, "No interface");
        mappings.put(0x80004003L, "Invalid pointer");
        mappings.put(0x80004004L, "Operation aborted");
        mappings.put(0x80004005L, "Unspecified failure");
        mappings.put(0x80070005L, "Access denied");
        mappings.put(0x8007000EL, "Out of memory");
        mappings.put(0x80070057L, "Invalid argument");
        
        // WIA-specific error codes
        mappings.put(0x80210001L, "General error");
        mappings.put(0x80210002L, "Paper jam");
        mappings.put(0x80210003L, "Paper empty");
        mappings.put(0x80210004L, "Paper problem");
        mappings.put(0x80210005L, "Device offline");
        mappings.put(0x80210006L, "Device busy");
        mappings.put(0x80210007L, "Warming up");
        mappings.put(0x80210008L, "User intervention required");
        mappings.put(0x80210009L, "Item deleted");
        mappings.put(0x8021000AL, "Device communication error");
        mappings.put(0x8021000BL, "Invalid command");
        mappings.put(0x8021000CL, "Incorrect hardware setting");
        mappings.put(0x8021000DL, "Device locked");
        mappings.put(0x8021000EL, "Exception in driver");
        mappings.put(0x8021000FL, "Invalid driver response");
        mappings.put(0x80210010L, "Cover open");
        mappings.put(0x80210011L, "Lamp off");
        mappings.put(0x80210012L, "Destination unreachable");
        mappings.put(0x80210013L, "Network reservation failed");
        mappings.put(0x80210014L, "Multiple feed");
        mappings.put(0x80210015L, "Device stopped");
        
        return mappings;
    }

    /**
     * Initializes error message templates for localization.
     */
    private Map<String, String> initializeMessageTemplates() {
        Map<String, String> templates = new HashMap<>();
        
        // TWAIN message templates
        templates.put("TWAIN_1", "Scanner operation failed. Please check device connection and try again.");
        templates.put("TWAIN_2", "Scanner status check required. Device may need attention.");
        templates.put("TWAIN_3", "Scanner operation was cancelled by user or system.");
        templates.put("TWAIN_4", "No more scanner devices available for enumeration.");
        templates.put("TWAIN_7", "Scanner is currently busy. Please wait and try again.");
        templates.put("TWAIN_8", "Scanner is locked by another application.");
        
        // WIA message templates
        templates.put("WIA_80210002", "Paper jam detected. Please clear the paper path and try again.");
        templates.put("WIA_80210003", "Paper tray is empty. Please load paper and try again.");
        templates.put("WIA_80210005", "Scanner is offline. Please check power and connection.");
        templates.put("WIA_80210006", "Scanner is busy. Please wait for current operation to complete.");
        templates.put("WIA_80210008", "User intervention required. Please check scanner display or status.");
        templates.put("WIA_8021000A", "Communication error with scanner. Please check connection.");
        templates.put("WIA_8021000D", "Scanner is locked by another application.");
        templates.put("WIA_80210010", "Scanner cover is open. Please close the cover and try again.");
        
        return templates;
    }
}