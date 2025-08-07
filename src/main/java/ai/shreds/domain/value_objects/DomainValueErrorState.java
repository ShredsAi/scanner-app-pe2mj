package ai.shreds.domain.value_objects;

import java.time.Instant;
import java.util.Objects;

/**
 * Value object representing an error state with code, message and timestamp.
 */
public class DomainValueErrorState {
    private final String errorCode;
    private final String errorMessage;
    private final Instant errorTimestamp;

    public DomainValueErrorState(String errorCode, String errorMessage) {
        if (errorCode == null || errorCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Error code cannot be null or empty");
        }
        if (errorMessage == null || errorMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Error message cannot be null or empty");
        }
        this.errorCode = errorCode.trim();
        this.errorMessage = errorMessage.trim();
        this.errorTimestamp = Instant.now();
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getErrorTimestamp() {
        return errorTimestamp;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        DomainValueErrorState that = (DomainValueErrorState) other;
        return Objects.equals(errorCode, that.errorCode) &&
               Objects.equals(errorMessage, that.errorMessage) &&
               Objects.equals(errorTimestamp, that.errorTimestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, errorMessage, errorTimestamp);
    }

    @Override
    public String toString() {
        return "DomainValueErrorState{" +
               "errorCode='" + errorCode + '\'' +
               ", errorMessage='" + errorMessage + '\'' +
               ", errorTimestamp=" + errorTimestamp +
               '}';
    }
}