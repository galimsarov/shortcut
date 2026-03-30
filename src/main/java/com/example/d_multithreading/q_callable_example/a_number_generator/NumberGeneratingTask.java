package com.example.d_multithreading.q_callable_example.a_number_generator;

import java.util.concurrent.Callable;

import static java.util.concurrent.ThreadLocalRandom.current;

public final class NumberGeneratingTask implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        return current().nextInt();
    }
}
