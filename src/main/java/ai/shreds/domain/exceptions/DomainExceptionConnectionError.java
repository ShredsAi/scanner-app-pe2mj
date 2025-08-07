package ai.shreds.domain.exceptions;

import ai.shreds.shared.enums.SharedEnumProtocolType;

/**
 * Domain exception thrown when device connection errors occur.
 */
public class DomainExceptionConnectionError extends RuntimeException {
    private final String connectionId;
    private final SharedEnumProtocolType protocol;

    public DomainExceptionConnectionError(String message, String connectionId, SharedEnumProtocolType protocol) {
        super(message);
        this.connectionId = connectionId;
        this.protocol = protocol;
    }

    public DomainExceptionConnectionError(String message, String connectionId, SharedEnumProtocolType protocol, Throwable cause) {
        super(message, cause);
        this.connectionId = connectionId;
        this.protocol = protocol;
    }

    public String getConnectionId() {
        return connectionId;
    }

    public SharedEnumProtocolType getProtocol() {
        return protocol;
    }

    @Override
    public String toString() {
        return "DomainExceptionConnectionError{" +
               "connectionId='" + connectionId + '\'' +
               ", protocol=" + protocol +
               ", message='" + getMessage() + '\'' +
               '}';
    }
}