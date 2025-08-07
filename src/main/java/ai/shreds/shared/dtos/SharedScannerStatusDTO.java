package ai.shreds.shared.dtos;

import ai.shreds.shared.enums.SharedEnumConnectionState;
import ai.shreds.shared.enums.SharedEnumAvailabilityState;
import ai.shreds.domain.entities.DomainEntityScannerStatus;
import ai.shreds.domain.value_objects.DomainValueErrorState;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Objects;

/**
 * Data Transfer Object representing scanner status information.
 */
public class SharedScannerStatusDTO {
    
    @JsonProperty("connectionState")
    private SharedEnumConnectionState connectionState;
    
    @JsonProperty("availability")
    private SharedEnumAvailabilityState availability;
    
    @JsonProperty("lastSeen")
    private Instant lastSeen;
    
    @JsonProperty("errorState")
    private String errorState;
    
    @JsonProperty("errorMessage")
    private String errorMessage;
    
    @JsonProperty("errorCode")
    private String errorCode;
    
    // Default constructor for JSON deserialization
    public SharedScannerStatusDTO() {}
    
    // Constructor with all fields
    public SharedScannerStatusDTO(SharedEnumConnectionState connectionState,
                                 SharedEnumAvailabilityState availability,
                                 Instant lastSeen,
                                 String errorState,
                                 String errorMessage,
                                 String errorCode) {
        this.connectionState = connectionState;
        this.availability = availability;
        this.lastSeen = lastSeen;
        this.errorState = errorState;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
    
    /**
     * Converts this DTO to a domain entity.
     * @return DomainEntityScannerStatus representing this DTO
     */
    public DomainEntityScannerStatus toEntity() {
        DomainValueErrorState domainErrorState = null;
        if (errorCode != null && errorMessage != null) {
            domainErrorState = new DomainValueErrorState(errorCode, errorMessage);
        }
        
        return new DomainEntityScannerStatus(
            connectionState != null ? connectionState : SharedEnumConnectionState.DISCONNECTED,
            availability != null ? availability : SharedEnumAvailabilityState.OFFLINE,
            lastSeen != null ? lastSeen : Instant.now(),
            domainErrorState,
            null // connection will be set separately if needed
        );
    }
    
    /**
     * Creates a DTO from a domain entity.
     * @param status the domain entity to convert
     * @return SharedScannerStatusDTO representing the domain entity
     */
    public static SharedScannerStatusDTO fromEntity(DomainEntityScannerStatus status) {
        if (status == null) {
            return null;
        }
        
        String errorCode = null;
        String errorMessage = null;
        String errorState = null;
        
        if (status.getErrorState() != null) {
            errorCode = status.getErrorState().getErrorCode();
            errorMessage = status.getErrorState().getErrorMessage();
            errorState = "ERROR";
        }
        
        return new SharedScannerStatusDTO(
            status.getConnectionState(),
            status.getAvailability(),
            status.getLastSeen(),
            errorState,
            errorMessage,
            errorCode
        );
    }
    
    // Getters and Setters
    public SharedEnumConnectionState getConnectionState() { return connectionState; }
    public void setConnectionState(SharedEnumConnectionState connectionState) { this.connectionState = connectionState; }
    
    public SharedEnumAvailabilityState getAvailability() { return availability; }
    public void setAvailability(SharedEnumAvailabilityState availability) { this.availability = availability; }
    
    public Instant getLastSeen() { return lastSeen; }
    public void setLastSeen(Instant lastSeen) { this.lastSeen = lastSeen; }
    
    public String getErrorState() { return errorState; }
    public void setErrorState(String errorState) { this.errorState = errorState; }
    
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedScannerStatusDTO)) return false;
        SharedScannerStatusDTO that = (SharedScannerStatusDTO) o;
        return connectionState == that.connectionState &&
               availability == that.availability &&
               Objects.equals(lastSeen, that.lastSeen) &&
               Objects.equals(errorState, that.errorState) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(errorCode, that.errorCode);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(connectionState, availability, lastSeen, errorState, errorMessage, errorCode);
    }
    
    @Override
    public String toString() {
        return "SharedScannerStatusDTO{" +
               "connectionState=" + connectionState +
               ", availability=" + availability +
               ", lastSeen=" + lastSeen +
               ", errorState='" + errorState + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               ", errorCode='" + errorCode + '\'' +
               '}';
    }
}