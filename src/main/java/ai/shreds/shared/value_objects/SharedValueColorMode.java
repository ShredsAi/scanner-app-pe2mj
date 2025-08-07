package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedEnumColorModeType;
import java.util.Objects;

/**
 * Immutable value object representing a color mode with its bit depth.
 */
public final class SharedValueColorMode {
    private final SharedEnumColorModeType mode;
    private final int bitDepth;
    private final boolean isDefault;

    /**
     * Constructs a ColorMode value object.
     * @param mode the color mode type
     * @param bitDepth the bit depth (1, 8, 16, 24, 32)
     */
    public SharedValueColorMode(SharedEnumColorModeType mode, int bitDepth) {
        this(mode, bitDepth, false);
    }

    /**
     * Constructs a ColorMode value object with default flag.
     * @param mode the color mode type
     * @param bitDepth the bit depth (1, 8, 16, 24, 32)
     * @param isDefault whether this is the default color mode
     */
    public SharedValueColorMode(SharedEnumColorModeType mode, int bitDepth, boolean isDefault) {
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
        if (bitDepth <= 0 || bitDepth > 32) {
            throw new IllegalArgumentException("bitDepth must be between 1 and 32");
        }
        this.bitDepth = bitDepth;
        this.isDefault = isDefault;
    }

    public SharedEnumColorModeType getMode() {
        return mode;
    }

    public int getBitDepth() {
        return bitDepth;
    }

    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SharedValueColorMode)) return false;
        SharedValueColorMode that = (SharedValueColorMode) other;
        return bitDepth == that.bitDepth &&
               isDefault == that.isDefault &&
               mode == that.mode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, bitDepth, isDefault);
    }

    @Override
    public String toString() {
        return "SharedValueColorMode{" +
               "mode=" + mode +
               ", bitDepth=" + bitDepth +
               ", isDefault=" + isDefault +
               '}';
    }
}