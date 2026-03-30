package com.example.d_multithreading.q_callable_example.b_coordinates.model;

import java.util.Objects;

public final class Area {
    private final Coordinate leftBottom;
    private final Coordinate rightUpper;

    public Area(final Coordinate leftBottom, final Coordinate rightUpper) {
        this.leftBottom = leftBottom;
        this.rightUpper = rightUpper;
    }

    public Coordinate getLeftBottom() {
        return leftBottom;
    }

    public Coordinate getRightUpper() {
        return rightUpper;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Area area = (Area) o;
        return Objects.equals(leftBottom, area.leftBottom) && Objects.equals(rightUpper, area.rightUpper);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftBottom, rightUpper);
    }

    @Override
    public String toString() {
        return "Area{" +
                "leftBottom=" + leftBottom +
                ", rightUpper=" + rightUpper +
                '}';
    }
}
