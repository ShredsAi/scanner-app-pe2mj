package ai.shreds.shared.value_objects;

import java.util.Objects;

/**
 * Immutable value object representing scanning resolution with horizontal and vertical DPI values.
 */
public final class SharedValueResolution {
    private final int horizontalDpi;
    private final int verticalDpi;
    private final boolean isDefault;

    /**
     * Constructs a resolution value object. Defaults isDefault to false.
     * @param horizontalDpi horizontal DPI (50-9600)
     * @param verticalDpi vertical DPI (50-9600)
     */
    public SharedValueResolution(int horizontalDpi, int verticalDpi) {
        this(horizontalDpi, verticalDpi, false);
    }

    /**
     * Constructs a resolution value object with default flag.
     * @param horizontalDpi horizontal DPI (50-9600)
     * @param verticalDpi vertical DPI (50-9600)
     * @param isDefault whether this resolution is default
     */
    public SharedValueResolution(int horizontalDpi, int verticalDpi, boolean isDefault) {
        if (horizontalDpi < 50 || horizontalDpi > 9600) {
            throw new IllegalArgumentException("horizontalDpi must be between 50 and 9600");
        }
        if (verticalDpi < 50 || verticalDpi > 9600) {
            throw new IllegalArgumentException("verticalDpi must be between 50 and 9600");
        }
        this.horizontalDpi = horizontalDpi;
        this.verticalDpi = verticalDpi;
        this.isDefault = isDefault;
    }

    public int getHorizontalDpi() {
        return horizontalDpi;
    }

    public int getVerticalDpi() {
        return verticalDpi;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedValueResolution)) return false;
        SharedValueResolution that = (SharedValueResolution) o;
        return horizontalDpi == that.horizontalDpi &&
               verticalDpi == that.verticalDpi &&
               isDefault == that.isDefault;
    }

    @Override
    public int hashCode() {
        return Objects.hash(horizontalDpi, verticalDpi, isDefault);
    }

    @Override
    public String toString() {
        return "SharedValueResolution{" +
               "horizontalDpi=" + horizontalDpi +
               ", verticalDpi=" + verticalDpi +
               ", isDefault=" + isDefault +
               '}';
    }
}
