package com.example.d_multithreading.v_proselyte_experiments.c_collections.d_priority_queue;

import java.util.concurrent.BlockingQueue;

import static java.lang.Thread.currentThread;

public class WriteToPriorityQueueWorker implements Runnable {
    private final BlockingQueue<Integer> queue;

    public WriteToPriorityQueueWorker(final BlockingQueue<Integer> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try {
            this.queue.put(4);
            this.queue.put(3);
            this.queue.put(1);
            this.queue.put(2);
            this.queue.put(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
