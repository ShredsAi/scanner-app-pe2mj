package ai.shreds.domain.exceptions;

/**
 * Domain exception thrown when a scanner is in an invalid state for the requested operation.
 */
public class DomainExceptionInvalidScannerState extends RuntimeException {
    private final String currentState;
    private final String attemptedOperation;

    public DomainExceptionInvalidScannerState(String message, String currentState, String attemptedOperation) {
        super(message);
        this.currentState = currentState;
        this.attemptedOperation = attemptedOperation;
    }

    public DomainExceptionInvalidScannerState(String message, String currentState, String attemptedOperation, Throwable cause) {
        super(message, cause);
        this.currentState = currentState;
        this.attemptedOperation = attemptedOperation;
    }

    public String getCurrentState() {
        return currentState;
    }

    public String getAttemptedOperation() {
        return attemptedOperation;
    }

    @Override
    public String toString() {
        return "DomainExceptionInvalidScannerState{" +
               "currentState='" + currentState + '\'' +
               ", attemptedOperation='" + attemptedOperation + '\'' +
               ", message='" + getMessage() + '\'' +
               '}';
    }
}