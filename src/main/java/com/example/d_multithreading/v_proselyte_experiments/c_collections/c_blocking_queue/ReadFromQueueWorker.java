package com.example.d_multithreading.v_proselyte_experiments.c_collections.c_blocking_queue;

import java.util.concurrent.BlockingQueue;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ReadFromQueueWorker implements Runnable {
    private final BlockingQueue<Integer> queue;

    public ReadFromQueueWorker(final BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                int counter = queue.take();
                out.printf("Take: %d%n", counter);
                MILLISECONDS.sleep(150);
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
