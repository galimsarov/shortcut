package com.example.d_multithreading.q_callable_example.b_coordinates;

import com.example.d_multithreading.q_callable_example.b_coordinates.model.Area;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.City;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.Coordinate;
import com.example.d_multithreading.q_callable_example.b_coordinates.service.CityClient;
import com.example.d_multithreading.q_callable_example.b_coordinates.service.CityScanner;

import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;

public class Runner {
    public static void main(final String[] args) {
        try (final ExecutorService executorService = newFixedThreadPool(3)) {
            final CityScanner scanner = new CityScanner(new CityClient(), executorService);
            final Area area = new Area(new Coordinate(1, 1), new Coordinate(5.3, 5.3));
            final Set<City> cities = scanner.scan(area);
            System.out.println(cities);
        }
    }
}
