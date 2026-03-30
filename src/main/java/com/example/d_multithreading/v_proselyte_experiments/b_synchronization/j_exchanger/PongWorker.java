package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.j_exchanger;

import java.util.concurrent.Exchanger;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class PongWorker implements Runnable {
    private final AtomicInteger counter;
    private final Exchanger<AtomicInteger> exchanger;

    public PongWorker(final AtomicInteger counter, final Exchanger<AtomicInteger> exchanger) {
        this.counter = counter;
        this.exchanger = exchanger;
    }

    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                AtomicInteger exchangedCounter = exchanger.exchange(counter);
                out.println("PONG: " + exchangedCounter.getAndIncrement());
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
