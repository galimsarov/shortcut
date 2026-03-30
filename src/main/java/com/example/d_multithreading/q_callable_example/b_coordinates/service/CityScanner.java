package com.example.d_multithreading.q_callable_example.b_coordinates.service;

import com.example.d_multithreading.q_callable_example.b_coordinates.model.Area;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.City;
import com.example.d_multithreading.q_callable_example.b_coordinates.model.Coordinate;
import com.example.d_multithreading.q_callable_example.b_coordinates.util.FutureUtil;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import static com.example.d_multithreading.q_callable_example.b_coordinates.util.IteratorUtil.asList;
import static java.lang.Math.ceil;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.IntStream.range;

public final class CityScanner {
    private static final int TASK_POINT_COUNT = 5;

    private final CityClient client;
    private final ExecutorService executorService;

    public CityScanner(final CityClient client, final ExecutorService executorService) {
        this.client = client;
        this.executorService = executorService;
    }

    public Set<City> scan(final Area area) {
        final List<Coordinate> coordinates = asList(new AreaIterator(area));
        return range(0, countTasks(coordinates))
                .mapToObj(i -> getTaskCoordinates(coordinates, i))
                .map(ScanningTask::new)
                .map(executorService::submit)
                .toList()
                .stream()
                .map(FutureUtil::get)
                .flatMap(Collection::stream)
                .collect(toUnmodifiableSet());
    }

    private List<Coordinate> getTaskCoordinates(final List<Coordinate> coordinates, final int taskIndex) {
        final int fromIndex = taskIndex * TASK_POINT_COUNT;
        final int toIndex = min(TASK_POINT_COUNT * (taskIndex + 1), coordinates.size());
        return coordinates.subList(fromIndex, toIndex);
    }

    private int countTasks(final List<Coordinate> coordinates) {
        return (int) ceil(((double) coordinates.size()) / TASK_POINT_COUNT);
    }


    private final class ScanningTask implements Callable<Set<City>> {
        private final List<Coordinate> coordinates;

        public ScanningTask(final List<Coordinate> coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public Set<City> call() {
            return coordinates.stream()
                    .map(client::request)
                    .collect(toUnmodifiableSet());
        }
    }
}
