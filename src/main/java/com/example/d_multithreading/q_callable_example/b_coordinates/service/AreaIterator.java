package com.example.d_multithreading.q_callable_example.b_coordinates.service;

import com.example.d_multithreading.q_callable_example.b_coordinates.model.Area;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.Coordinate;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Double.compare;
import static java.lang.Math.min;

public final class AreaIterator implements Iterator<Coordinate> {
    private static final double STEP = 0.5;

    private final Area area;
    private Coordinate cursor;

    public AreaIterator(final Area area) {
        this.area = area;
        setCursorBeforeFirst(area);
    }

    @Override
    public boolean hasNext() {
        return hasNextLatitude() || hasNextLongitude();
    }

    @Override
    public Coordinate next() {
        if (hasNextLatitude()) {
            return nextLatitude();
        } else if (hasNextLongitude()) {
            return nextLongitude();
        }
        throw new NoSuchElementException();
    }

    private void setCursorBeforeFirst(final Area area) {
        final double latitude = area.getLeftBottom().getLatitude() - STEP;
        final double longitude = area.getLeftBottom().getLongitude();
        cursor = new Coordinate(latitude, longitude);
    }

    private boolean hasNextLatitude() {
        return compare(cursor.getLatitude(), area.getRightUpper().getLatitude()) < 0;
    }

    private boolean hasNextLongitude() {
        return compare(cursor.getLongitude(), area.getRightUpper().getLongitude()) < 0;
    }

    private Coordinate nextLatitude() {
        final double next = min(cursor.getLatitude() + STEP, area.getRightUpper().getLatitude());
        cursor = new Coordinate(next, cursor.getLongitude());
        return cursor;
    }

    private Coordinate nextLongitude() {
        final double next = min(cursor.getLongitude() + STEP, area.getRightUpper().getLongitude());
        cursor = new Coordinate(area.getLeftBottom().getLatitude(), next);
        return cursor;
    }
}
