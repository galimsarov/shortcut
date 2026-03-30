package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.f_reentrant_lock_demo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class ReentrantLockDemo {
    private static Integer counter = 0;
    private static final Lock counterLock = new ReentrantLock();
    public static void main(String[] args) {
        long start = currentTimeMillis();
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                incrementCounter();
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                incrementCounter();
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

    public static void incrementCounter() {
        counterLock.lock();
        try {
            counter++;
        } finally {
            counterLock.unlock();
        }
    }
}
