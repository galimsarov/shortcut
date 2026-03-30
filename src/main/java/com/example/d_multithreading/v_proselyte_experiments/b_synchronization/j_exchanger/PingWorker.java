package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.j_exchanger;

import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class PingWorker implements Runnable {
    private final AtomicInteger counter;
    private final Exchanger<AtomicInteger> exchanger;

    public PingWorker(final AtomicInteger counter, final Exchanger<AtomicInteger> exchanger) {
        this.counter = counter;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                AtomicInteger atomicInteger = exchanger.exchange(counter);
                out.println("PING: " + atomicInteger.getAndIncrement());
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
