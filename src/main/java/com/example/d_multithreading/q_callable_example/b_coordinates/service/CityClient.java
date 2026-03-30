package com.example.d_multithreading.q_callable_example.b_coordinates.service;

import com.example.d_multithreading.q_callable_example.b_coordinates.model.City;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.Coordinate;

import static java.util.concurrent.ThreadLocalRandom.current;
import static java.util.concurrent.TimeUnit.SECONDS;


public final class CityClient {
    private static final long SECOND_TIMEOUT = 1;
    private static final long MIN_ID = 0;
    private static final long MAX_ID = 10;

    public City request(@SuppressWarnings("unused") final Coordinate coordinate) {
        try {
            SECONDS.sleep(SECOND_TIMEOUT);
            final long id = current().nextLong(MIN_ID, MAX_ID + 1);
            return new City(id);
        } catch (final InterruptedException exception) {
            throw new RuntimeException(exception);
        }
    }
}
