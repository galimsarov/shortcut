package com.example.d_multithreading.s_virtual;

import java.util.concurrent.ExecutorService;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;

public class Runner {
    private static final Object LOCK = new Object();

    public static void main(String[] args) {
        try (final ExecutorService executor = newVirtualThreadPerTaskExecutor()) {
            range(0, 3)
                    .forEach(i -> executor.submit(
                            () -> {
                                synchronized (LOCK) {
                                    out.println("Task " + i + " started on " + currentThread());
                                    executeIO();
                                    out.println("Task " + i + " finished on " + currentThread());
                                }
                            }));
        }
    }

    private static void executeIO() {
        try {
            SECONDS.sleep(2);
        } catch (InterruptedException exception) {
            currentThread().interrupt();
        }
    }
}
