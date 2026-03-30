package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.m_starvation_demo;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class StarvationDemo {
    private static final AtomicInteger atomicInteger = new AtomicInteger(0);

    public static void main(String[] args) {
        out.printf("%s execution starts%n", currentThread().getName());

        StarvationWorker thread1 = new StarvationWorker(atomicInteger, "T1");
        thread1.setPriority(10);

        StarvationWorker thread2 = new StarvationWorker(atomicInteger, "T2");
        thread2.setPriority(9);

        StarvationWorker thread3 = new StarvationWorker(atomicInteger, "T3");
        thread3.setPriority(8);

        StarvationWorker thread4 = new StarvationWorker(atomicInteger, "T4");
        thread4.setPriority(7);

        StarvationWorker thread5 = new StarvationWorker(atomicInteger, "T5");
        thread5.setPriority(1);

        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
        thread5.start();

        out.printf("%s execution completes%n", currentThread().getName());
    }
}
