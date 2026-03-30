package com.example.d_multithreading.v_proselyte_experiments.d_executors.a_single_thread_pool;

import java.util.Random;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class GenerateRandomIntegerTask implements Runnable {
    private final Random random;

    public GenerateRandomIntegerTask() {
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            MILLISECONDS.sleep(100);
            final int randomInt = this.random.nextInt(1000) + 1;
            out.printf("SingleThreadPoolTask: %d%n", randomInt);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
