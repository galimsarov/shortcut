package com.example.d_multithreading.u_experiments.executor_service.a_manual;

import static java.lang.System.out;
import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(final String[] args) {
        final ExecutorService executorService = new ExecutorService(2);
        range(0, 3).forEach(i -> executorService.execute(() -> out.println("Test message")));
    }
}
