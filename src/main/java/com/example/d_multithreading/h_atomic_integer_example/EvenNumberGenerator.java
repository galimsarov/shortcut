package com.example.d_multithreading.h_atomic_integer_example;

import java.util.concurrent.atomic.AtomicInteger;

public class EvenNumberGenerator {
    private static final int GENERATION_DELTA = 2;

    private final AtomicInteger value = new AtomicInteger();

    public int generate() {
        return this.value.getAndAdd(GENERATION_DELTA);
    }

    public int getValue() {
        return this.value.intValue();
    }
}
