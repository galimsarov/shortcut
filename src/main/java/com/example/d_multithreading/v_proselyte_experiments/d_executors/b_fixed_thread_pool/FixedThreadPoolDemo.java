package com.example.d_multithreading.v_proselyte_experiments.d_executors.b_fixed_thread_pool;

import java.util.concurrent.ExecutorService;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.IntStream.range;

public class FixedThreadPoolDemo {
    public static void main(String[] args) {
        final int cores = getRuntime().availableProcessors();
        final long start = currentTimeMillis();
        out.printf("Cores: %d%n", cores);
        try (ExecutorService executorService = newFixedThreadPool(cores - 1)) {
            range(0, 100).forEach(i ->
                    executorService.execute(new GenerateRandomIntegerTask())
            );
        } finally {
            long end = currentTimeMillis();
            long duration = end - start;
            out.printf("Processed in: %d ms%n", duration);
        }
    }
}
