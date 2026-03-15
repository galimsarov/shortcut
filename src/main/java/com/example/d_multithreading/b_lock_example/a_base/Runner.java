package com.example.d_multithreading.b_lock_example.a_base;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;
import static java.lang.Thread.*;
import static java.util.stream.IntStream.range;

public class Runner {
    public static void main(String[] args) {
        final EvenNumberGenerator evenNumberGenerator = new EvenNumberGenerator();

        final Runnable generatingTask = () -> range(0, 100).forEach(i -> out.println(evenNumberGenerator.generate()));

        final Thread firstThread = new Thread(generatingTask);
        firstThread.start();

        final Thread secondThread = new Thread(generatingTask);
        secondThread.start();

        final Thread thirdThread = new Thread(generatingTask);
        thirdThread.start();

    }

    private static final class EvenNumberGenerator {
        private final Lock lock;
        private int previousGenerated;

        public EvenNumberGenerator() {
            this.lock = new ReentrantLock();
            this.previousGenerated = -2;
        }

        public int generate() {
            return this.lock.tryLock()
                    ? this.onSuccessAcquireLock()
                    : this.onFailAcquireLock();
        }

        private int onSuccessAcquireLock() {
            try {
                return this.previousGenerated += 2;
            } finally {
                this.lock.unlock();
            }
        }

        private int onFailAcquireLock() {
            out.printf("Thread '%s' didn't acquire lock\n", currentThread().getName());
            throw new RuntimeException();
        }
    }
}
