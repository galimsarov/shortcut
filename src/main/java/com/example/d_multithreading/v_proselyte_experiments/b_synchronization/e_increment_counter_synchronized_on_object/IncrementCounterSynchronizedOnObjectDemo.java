package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.e_increment_counter_synchronized_on_object;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class IncrementCounterSynchronizedOnObjectDemo {
    private static Integer counter = 0;
    private static Integer anotherCounter = 0;

    private static final Object counterLock = new Object();
    private static final Object anotherCounterLock = new Object();

    public static void main(String[] args) {
        long start = currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                incrementCounter();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                incrementAnotherCounter();
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
        out.println("Another Counter: " + anotherCounter);
        out.println("Time elapsed: " + duration);
    }

    private static void incrementCounter() {
        synchronized (counterLock) {
            counter++;
        }
    }

    private static void incrementAnotherCounter() {
        synchronized (anotherCounterLock) {
            anotherCounter++;
        }
    }
}
