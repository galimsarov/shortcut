package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.n_atomic_long;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicLong;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class AtomicLongDemo {
    private static final AtomicLong atomicLongCounter = new AtomicLong();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
           for (int i = 0; i < 1_000_000; i++) {
               incrementAtomicLong();
           }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                incrementAtomicLong();
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
        out.printf("atomicLongCounter: %d%n", atomicLongCounter.get());
    }

    private static void incrementAtomicLong() {
        atomicLongCounter.incrementAndGet();
    }
}
