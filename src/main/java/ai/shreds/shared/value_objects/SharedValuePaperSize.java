package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedEnumMeasurementUnit;
import ai.shreds.shared.enums.SharedEnumStandardPaperSize;
import java.util.Objects;

/**
 * Immutable value object representing paper dimensions and standard size information.
 */
public final class SharedValuePaperSize {
    private final double width;
    private final double height;
    private final SharedEnumMeasurementUnit unit;
    private final SharedEnumStandardPaperSize standardSize;
    private final int widthMm;
    private final int heightMm;
    private final boolean isDefault;

    /**
     * Constructs a PaperSize value object.
     */
    public SharedValuePaperSize(double width, double height, SharedEnumMeasurementUnit unit, SharedEnumStandardPaperSize standardSize, boolean isDefault) {
        if (width <= 0) throw new IllegalArgumentException("width must be positive");
        if (height <= 0) throw new IllegalArgumentException("height must be positive");
        this.width = width;
        this.height = height;
        this.unit = Objects.requireNonNull(unit, "unit cannot be null");
        this.standardSize = Objects.requireNonNull(standardSize, "standardSize cannot be null");
        this.isDefault = isDefault;
        // compute mm dimensions
        if (unit == SharedEnumMeasurementUnit.INCHES) {
            this.widthMm = (int) Math.round(width * 25.4);
            this.heightMm = (int) Math.round(height * 25.4);
        } else {
            this.widthMm = (int) Math.round(width);
            this.heightMm = (int) Math.round(height);
        }
    }

    public double getWidth() { return width; }
    public double getHeight() { return height; }
    public SharedEnumMeasurementUnit getUnit() { return unit; }
    public SharedEnumStandardPaperSize getStandardSize() { return standardSize; }
    public int getWidthMm() { return widthMm; }
    public int getHeightMm() { return heightMm; }
    public boolean isDefault() { return isDefault; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedValuePaperSize)) return false;
        SharedValuePaperSize that = (SharedValuePaperSize) o;
        return Double.compare(that.width, width) == 0 &&
               Double.compare(that.height, height) == 0 &&
               widthMm == that.widthMm &&
               heightMm == that.heightMm &&
               isDefault == that.isDefault &&
               unit == that.unit &&
               standardSize == that.standardSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, unit, standardSize, widthMm, heightMm, isDefault);
    }

    @Override
    public String toString() {
        return "SharedValuePaperSize{" +
               "width=" + width +
               ", height=" + height +
               ", unit=" + unit +
               ", standardSize=" + standardSize +
               ", widthMm=" + widthMm +
               ", heightMm=" + heightMm +
               ", isDefault=" + isDefault +
               '}';
    }
}
