package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.m_starvation_demo;

import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;

public class StarvationWorker extends Thread {
    private final AtomicInteger threadCount;

    public StarvationWorker(final AtomicInteger threadCount, final String name) {
        this.threadCount = threadCount;
        this.setName(name);
    }

    @Override
    public void run() {
        this.threadCount.incrementAndGet();
        out.printf(
                "Thread: %s, counter: %d thread execution starts%n",
                this.getName(),
                this.threadCount.get()
        );
        out.printf(
                "Thread: %s thread execution completes%n",
                this.getName()
        );
    }
}
