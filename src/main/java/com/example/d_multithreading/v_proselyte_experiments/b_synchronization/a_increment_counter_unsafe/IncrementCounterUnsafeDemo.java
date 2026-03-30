package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.a_increment_counter_unsafe;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class IncrementCounterUnsafeDemo {
    private static int counter = 0;

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
                increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10_000; i++) {
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
        out.println("Counter: " + counter);
    }

    private static void increment() {
        counter++;
    }
}
