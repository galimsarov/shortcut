package com.example.d_multithreading.d_reentrant_read_write_lock_example;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.IntStream.range;

// CounterGuardedByLock - 429 176 407
// CounterGuardedByReadWriteLock - 45 201 679

// CounterGuardedByLock - 54
// CounterGuardedByReadWriteLock - 166

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        testCounter(CounterGuardedByReadWriteLock::new);
    }

    private static void testCounter(final Supplier<? extends AbstractCounter> counterFactory) throws InterruptedException {
        final AbstractCounter counter = counterFactory.get();

        final int amountOfThreadsGettingValue = 50;
        final ReadingValueTask[] readingValueTasks = createReadingTasks(counter, amountOfThreadsGettingValue);
        final Thread[] readingValueThreads = mapToThreads(readingValueTasks);

        final Runnable incrementingCounterTask = createIncrementingCounterTask(counter);
        final int amountOfThreadsIncrementingCounter = 2;
        final Thread[] incrementingCounterThreads =
                createThreads(incrementingCounterTask, amountOfThreadsIncrementingCounter);

        startThreads(readingValueThreads);
        startThreads(incrementingCounterThreads);

        SECONDS.sleep(5);

        interruptThreads(readingValueThreads);
        interruptThreads(incrementingCounterThreads);

        waitUntilFinish(readingValueThreads);

        final long totalAmountOfReads = findTotalAmountOfThreads(readingValueTasks);
        out.printf("Amount of readings value: %d", totalAmountOfReads);
    }

    private static ReadingValueTask[] createReadingTasks(final AbstractCounter counter, final int amountOfTasks) {
        return range(0, amountOfTasks)
                .mapToObj(i -> new ReadingValueTask(counter))
                .toArray(ReadingValueTask[]::new);
    }

    private static Thread[] mapToThreads(final Runnable[] tasks) {
        return stream(tasks).map(Thread::new).toArray(Thread[]::new);
    }

    private static Runnable createIncrementingCounterTask(final AbstractCounter counter) {
        return () -> {
            while (!currentThread().isInterrupted()) {
                incrementCounter(counter);
            }
        };
    }

    private static void incrementCounter(final AbstractCounter counter) {
        try {
            counter.increment();
            SECONDS.sleep(1);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    private static Thread[] createThreads(final Runnable task, final int amountOfThreads) {
        return range(0, amountOfThreads)
                .mapToObj(i -> new Thread(task))
                .toArray(Thread[]::new);
    }

    private static void startThreads(final Thread[] threads) {
        forEach(threads, Thread::start);
    }

    private static void interruptThreads(final Thread[] threads) {
        forEach(threads, Thread::interrupt);
    }

    private static void forEach(final Thread[] threads, final Consumer<Thread> action) {
        stream(threads).forEach(action);
    }

    private static void waitUntilFinish(final Thread[] threads) {
        forEach(threads, Runner::waitUntilFinish);
    }

    private static void waitUntilFinish(final Thread thread) {
        try {
            thread.join();
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }

    private static long findTotalAmountOfThreads(final ReadingValueTask[] tasks) {
        return stream(tasks)
                .mapToLong(ReadingValueTask::getAmountOfReads)
                .sum();
    }

    private static final class ReadingValueTask implements Runnable {
        private final AbstractCounter counter;
        private long amountOfReads;

        public ReadingValueTask(final AbstractCounter counter) {
            this.counter = counter;
        }

        public long getAmountOfReads() {
            return this.amountOfReads;
        }

        @Override
        public void run() {
            while (!currentThread().isInterrupted()) {
                this.counter.getValue();
                this.amountOfReads++;
            }
        }
    }
}
