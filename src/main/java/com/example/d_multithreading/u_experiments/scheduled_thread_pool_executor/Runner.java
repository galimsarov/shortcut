package com.example.d_multithreading.u_experiments.scheduled_thread_pool_executor;

import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.SECONDS;

public class Runner {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        try (ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4)) {
//            executor.schedule(new Task(1, 5), 5, SECONDS);
            ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(new Task(1, 5), 5, 5, SECONDS);
            scheduledFuture.get();
        }


    }
}
