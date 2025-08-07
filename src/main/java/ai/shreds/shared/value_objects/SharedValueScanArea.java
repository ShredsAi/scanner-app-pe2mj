package ai.shreds.shared.value_objects;

import ai.shreds.shared.enums.SharedEnumMeasurementUnit;
import java.util.Objects;

/**
 * Immutable value object representing a rectangular scanning area.
 */
public final class SharedValueScanArea {
    private final SharedValuePoint topLeft;
    private final SharedValuePoint bottomRight;
    private final SharedEnumMeasurementUnit unit;

    public SharedValueScanArea(SharedValuePoint topLeft, SharedValuePoint bottomRight, SharedEnumMeasurementUnit unit) {
        this.topLeft = Objects.requireNonNull(topLeft, "topLeft cannot be null");
        this.bottomRight = Objects.requireNonNull(bottomRight, "bottomRight cannot be null");
        this.unit = Objects.requireNonNull(unit, "unit cannot be null");
        if (bottomRight.getX() <= topLeft.getX() || bottomRight.getY() <= topLeft.getY()) {
            throw new IllegalArgumentException("bottomRight must be greater than topLeft in both coordinates");
        }
    }

    public SharedValuePoint getTopLeft() { return topLeft; }
    public SharedValuePoint getBottomRight() { return bottomRight; }
    public SharedEnumMeasurementUnit getUnit() { return unit; }

    /**
     * @return width of scan area in specified unit
     */
    public double getWidth() {
        return bottomRight.getX() - topLeft.getX();
    }

    /**
     * @return height of scan area in specified unit
     */
    public double getHeight() {
        return bottomRight.getY() - topLeft.getY();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedValueScanArea)) return false;
        SharedValueScanArea that = (SharedValueScanArea) o;
        return topLeft.equals(that.topLeft) &&
               bottomRight.equals(that.bottomRight) &&
               unit == that.unit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLeft, bottomRight, unit);
    }

    @Override
    public String toString() {
        return "SharedValueScanArea{" +
               "topLeft=" + topLeft +
               ", bottomRight=" + bottomRight +
               ", unit=" + unit +
               '}';
    }
}
