package com.example.d_multithreading.u_experiments.executor_service.a_manual;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static java.lang.Thread.currentThread;
import static java.util.stream.IntStream.range;

public class ExecutorService {
    private final BlockingQueue<Runnable> tasks = new LinkedBlockingQueue<>();

    public ExecutorService(final int threadCount) {
        startThreads(threadCount);
    }

    public void execute(Runnable task) {
        try {
            tasks.put(task);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    private void startThreads(final int count) {
        range(0, count)
                .mapToObj(i -> new Thread(new TaskExecutor()))
                .forEach(Thread::start);
    }

    private final class TaskExecutor implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    tasks.take().run();
                }
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }


}
