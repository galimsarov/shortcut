package com.example.d_multithreading.v_proselyte_experiments.d_executors.a_single_thread_pool;

import java.util.concurrent.ExecutorService;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static java.util.stream.IntStream.range;

public class SingleThreadPoolDemo {
    public static void main(String[] args) {
        long start = currentTimeMillis();
        try (ExecutorService executorService = newSingleThreadExecutor()) {
            range(0, 100).forEach(i -> executorService.execute(new GenerateRandomIntegerTask())
            );
        } finally {
            long end = currentTimeMillis();
            long duration = end - start;
            out.printf("Processed in: %d ms%n", duration);
        }
    }
}
