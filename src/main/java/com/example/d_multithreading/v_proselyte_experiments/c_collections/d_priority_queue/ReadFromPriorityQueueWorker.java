package com.example.d_multithreading.v_proselyte_experiments.c_collections.d_priority_queue;

import java.util.concurrent.BlockingQueue;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ReadFromPriorityQueueWorker implements Runnable {
    private final BlockingQueue<Integer> queue;

    public ReadFromPriorityQueueWorker(final BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            SECONDS.sleep(3);
            for (int i = 0; i < 5; i++) {
                out.printf("Take: %d%n", queue.take());
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
