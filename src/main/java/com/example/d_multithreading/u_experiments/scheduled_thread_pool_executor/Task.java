package com.example.d_multithreading.u_experiments.scheduled_thread_pool_executor;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Task implements Runnable {
    private final long id;
    private final int durationTime;

    public Task(long id, int durationTime) {
        this.id = id;
        this.durationTime = durationTime;
    }

    @Override
    public void run() {
        try {
            out.println("task id: " + id + " duration: " + durationTime + " started with " + currentThread().getName());
            SECONDS.sleep(durationTime);
            out.println("task id: " + id + " duration: " + durationTime + " finished with " + currentThread().getName());
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
