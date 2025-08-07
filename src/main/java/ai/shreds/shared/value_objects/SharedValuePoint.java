package ai.shreds.shared.value_objects;

import java.util.Objects;

/**
 * Immutable value object representing a point coordinate.
 */
public final class SharedValuePoint {
    private final double x;
    private final double y;

    /**
     * Constructs a point with given coordinates.
     * @param x x-coordinate
     * @param y y-coordinate
     */
    public SharedValuePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SharedValuePoint)) return false;
        SharedValuePoint that = (SharedValuePoint) o;
        return Double.compare(that.x, x) == 0 &&
               Double.compare(that.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "SharedValuePoint{" +
               "x=" + x +
               ", y=" + y +
               '}';
    }
}
