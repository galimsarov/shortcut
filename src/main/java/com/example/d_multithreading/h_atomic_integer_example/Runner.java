package com.example.d_multithreading.h_atomic_integer_example;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(String[] args) {
        final EvenNumberGenerator generator = new EvenNumberGenerator();

        final int taskGenerationCounts = 10_000;
        final Runnable generatingTask = () -> range(0, taskGenerationCounts).forEach(i -> generator.generate());

        final int amountOfGeneratingThreads = 5;
        final Thread[] generatingThreads = createThreads(generatingTask, amountOfGeneratingThreads);

        startThreads(generatingThreads);
        waitUntilFinish(generatingThreads);

        final int expectedGeneratorValue = amountOfGeneratingThreads * taskGenerationCounts * 2;
        final int actualGeneratorValue = generator.getValue();
        if (expectedGeneratorValue != actualGeneratorValue) {
            throw new RuntimeException(
                    "Expected is %d but was %d".formatted(expectedGeneratorValue, actualGeneratorValue)
            );
        }
    }

    private static Thread[] createThreads(final Runnable task, final int amountOfThreads) {
        return range(0, amountOfThreads)
                .mapToObj(i -> new Thread(task))
                .toArray(Thread[]::new);
    }

    private static void startThreads(final Thread[] threads) {
        stream(threads).forEach(Thread::start);
    }

    private static void waitUntilFinish(final Thread[] threads) {
        stream(threads).forEach(Runner::waitUntilFinish);
    }

    private static void waitUntilFinish(final Thread thread) {
        try {
            thread.join();
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
