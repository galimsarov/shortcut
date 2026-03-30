package com.example.d_multithreading.v_proselyte_experiments.c_collections.c_blocking_queue;

import java.util.concurrent.BlockingQueue;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class WriteToQueueWorker implements Runnable {
    private final BlockingQueue<Integer> queue;

    public WriteToQueueWorker(final BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        int counter = 0;
        while (!currentThread().isInterrupted()) {
            try {
                out.printf("Put: %d%n", counter);
                this.queue.put(counter++);
                MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
