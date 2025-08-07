package ai.shreds.domain.exceptions;

import ai.shreds.shared.value_objects.SharedValueScannerId;

/**
 * Domain exception thrown when a scanner is not found.
 */
public class DomainExceptionScannerNotFound extends RuntimeException {
    private final SharedValueScannerId scannerId;

    public DomainExceptionScannerNotFound(SharedValueScannerId scannerId) {
        super("Scanner not found: " + (scannerId != null ? scannerId.toString() : "null"));
        this.scannerId = scannerId;
    }

    public DomainExceptionScannerNotFound(SharedValueScannerId scannerId, String message) {
        super(message);
        this.scannerId = scannerId;
    }

    public DomainExceptionScannerNotFound(SharedValueScannerId scannerId, String message, Throwable cause) {
        super(message, cause);
        this.scannerId = scannerId;
    }

    public SharedValueScannerId getScannerId() {
        return scannerId;
    }

    @Override
    public String toString() {
        return "DomainExceptionScannerNotFound{" +
               "scannerId=" + scannerId +
               ", message='" + getMessage() + '\'' +
               '}';
    }
}