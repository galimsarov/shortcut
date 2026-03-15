package com.example.d_multithreading.c_condition_example;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Stream.iterate;

public class Runner {
    public static void main(String[] args) {
        BoundedBuffer<Integer> boundedBuffer = new BoundedBuffer<>(5);

        final Runnable producingTask = () -> iterate(0, i -> i + 1)
                .forEach(i -> {
                    try {
                        boundedBuffer.put(i);
                        SECONDS.sleep(1);
                    } catch (InterruptedException e) {
                        currentThread().interrupt();
                    }
                });
        final Thread producingThread = new Thread(producingTask);

        final Runnable consumingTask = () -> {
            try {
                while (!currentThread().isInterrupted()) {
                    boundedBuffer.take();
                    SECONDS.sleep(3);
                }
            } catch (final InterruptedException e) {
                currentThread().interrupt();
            }
        };
        final Thread consumingThread = new Thread(consumingTask);

        producingThread.start();
        consumingThread.start();
    }
}
