package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.b_increment_counter_synchronized;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class IncrementCounterSynchronizedDemo {
    private static Integer counter = 0;

    public static void main(String[] args) {
        long start = currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                increment();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                increment();
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
        long end = currentTimeMillis();
        long duration = end - start;
        out.println("Counter: " + counter);
        out.println("Time elapsed: " + duration);
    }

    private static synchronized void increment() {
        counter++;
    }
}
