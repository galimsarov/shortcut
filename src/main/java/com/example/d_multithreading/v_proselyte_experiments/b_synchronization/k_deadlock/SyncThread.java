package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.k_deadlock;

import java.util.concurrent.TimeUnit;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class SyncThread implements Runnable {
    private final Object lock1;
    private final Object lock2;

    public SyncThread(final Object lock1, final Object lock2) {
        this.lock1 = lock1;
        this.lock2 = lock2;
    }

    @Override
    public void run() {
        String name = currentThread().getName();
        out.printf("Thread: %s acquiring lock on %s%n", name, lock1);
        synchronized (lock1) {
            out.printf("Thread: %s acquired lock on %s%n", name, lock1);
            work();
            out.printf("Thread: %s acquiring lock on %s%n", name, lock2);
            synchronized (lock2) {
                out.printf("Thread: %s acquired lock on %s%n", name, lock2);
                work();
            }
            out.printf("Thread: %s released lock on %s%n", name, lock2);
        }
        out.printf("Thread: %s released lock on %s%n", name, lock1);
        out.printf("Thread: %s finished execution%n", name);
    }

    private void work() {
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
