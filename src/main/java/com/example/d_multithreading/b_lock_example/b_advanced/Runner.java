package com.example.d_multithreading.b_lock_example.b_advanced;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.IntConsumer;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        final Counter counter = new Counter();

        final int incrementAmount = 10;
        final Thread incrementingThread = new Thread(
                createTaskDoingOperationOnCounter(counter, i -> counter.increment(), incrementAmount)
        );

        final int decrementAmount = 10;
        final Thread decrementingThread = new Thread(
                createTaskDoingOperationOnCounter(counter, i -> counter.decrement(), decrementAmount)
        );

        startThreads(incrementingThread, decrementingThread);
        waitUntilFinish(incrementingThread, decrementingThread);

        out.printf("Counter's value: %d\n", counter.getValue());
    }

    private static void startThreads(final Thread... threads) {
        stream(threads).forEach(Thread::start);
    }

    private static void waitUntilFinish(final Thread... threads) throws InterruptedException {
        for (final Thread thread : threads) {
            thread.join();
        }
    }

    private static Runnable createTaskDoingOperationOnCounter(
            final Counter counter, final IntConsumer operation, final int times
    ) {
        return () -> {
            counter.lock();
            try {
                range(0, times).forEach(operation);
            } finally {
                counter.unlock();
            }
        };
    }

    private static final class Counter {
        private final Lock lock;
        private int value;

        public Counter() {
            lock = new ReentrantLock();
        }

        public void lock() {
            this.lock.lock();
            printMessageWithCurrentThreadNameArgument("Thread '%s' locked counter\n");
        }

        public void increment() {
            this.value++;
            printMessageWithCurrentThreadNameArgument("Thread '%s' incremented counter\n");
        }

        public void decrement() {
            this.value--;
            printMessageWithCurrentThreadNameArgument("Thread '%s' decremented counter\n");
        }

        public void unlock() {
            printMessageWithCurrentThreadNameArgument("Thread '%s' is unlocking counter\n");
            this.lock.unlock();
        }

        public int getValue() {
            return this.value;
        }

        private static void printMessageWithCurrentThreadNameArgument(String message) {
            out.printf(message, currentThread().getName());
        }
    }
}
