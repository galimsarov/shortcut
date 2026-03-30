package com.example.d_multithreading.t_thread_local_example;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(String[] args) {
        final int threadCount = 4;
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
        final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();
        range(0, threadCount)
                .mapToObj(i -> new Thread(
                        () -> {
                            try {
                                threadLocal.set(i);
                                out.println(currentThread().getName() + " set: " + i);
                                countDownLatch.countDown();
                                countDownLatch.await();
                                out.println(currentThread().getName() + " get: " + threadLocal.get());
                            } catch (InterruptedException exception) {
                                currentThread().interrupt();
                            }
                        })
                ).forEach(Thread::start);
    }
}
