package ai.shreds.infrastructure.exceptions;

/**
 * Exception thrown when native driver library loading fails.
 * Provides specific information about the library that failed to load.
 */
public class InfrastructureExceptionDriverLoadException extends RuntimeException {

    private final String libraryName;
    private final String libraryPath;

    /**
     * Constructs a new driver load exception with the specified message, library name, and path.
     *
     * @param message the detail message
     * @param libraryName the name of the library that failed to load
     * @param libraryPath the path where the library was expected to be found
     */
    public InfrastructureExceptionDriverLoadException(String message, String libraryName, String libraryPath) {
        super(message);
        this.libraryName = libraryName;
        this.libraryPath = libraryPath;
    }

    /**
     * Constructs a new driver load exception with the specified message, library name, path, and cause.
     *
     * @param message the detail message
     * @param libraryName the name of the library that failed to load
     * @param libraryPath the path where the library was expected to be found
     * @param cause the cause of the exception
     */
    public InfrastructureExceptionDriverLoadException(String message, String libraryName, String libraryPath, Throwable cause) {
        super(message, cause);
        this.libraryName = libraryName;
        this.libraryPath = libraryPath;
    }

    /**
     * Gets the name of the library that failed to load.
     *
     * @return the library name
     */
    public String getLibraryName() {
        return libraryName;
    }

    /**
     * Gets the path where the library was expected to be found.
     *
     * @return the library path
     */
    public String getLibraryPath() {
        return libraryPath;
    }

    @Override
    public String toString() {
        return String.format("%s: %s (Library: %s, Path: %s)", 
            getClass().getSimpleName(), 
            getMessage(), 
            libraryName, 
            libraryPath != null ? libraryPath : "system path");
    }
}