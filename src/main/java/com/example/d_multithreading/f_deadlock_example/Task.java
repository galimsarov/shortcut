package com.example.d_multithreading.f_deadlock_example;

import java.util.concurrent.locks.Lock;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class Task implements Runnable {
    private static final String MESSAGE_TEMPLATE_TRY_ACQUIRE_LOCK = "Thread '%s' is trying to acquire lock '%s'\n";
    private static final String MESSAGE_TEMPLATE_SUCCESS_ACQUIRE_LOCK = "Thread '%s' acquired lock '%s'\n";
    private static final String MESSAGE_TEMPLATE_RELEASE_LOCK = "Thread '%s' released lock '%s'\n";

    private static final String NAME_FIRST_LOCK = "firstLock";
    private static final String NAME_SECOND_LOCK = "secondLock";

    private Lock firstLock;
    private Lock secondLock;

    public Task(Lock firstLock, Lock secondLock) {
        this.firstLock = firstLock;
        this.secondLock = secondLock;
    }

    @Override
    public void run() {
        final String currentThreadName = currentThread().getName();
        out.printf(MESSAGE_TEMPLATE_TRY_ACQUIRE_LOCK, currentThreadName, NAME_FIRST_LOCK);
        this.firstLock.lock();
        try {
            out.printf(MESSAGE_TEMPLATE_SUCCESS_ACQUIRE_LOCK, currentThreadName, NAME_FIRST_LOCK);
            MILLISECONDS.sleep(200);
            out.printf(MESSAGE_TEMPLATE_TRY_ACQUIRE_LOCK, currentThreadName, NAME_SECOND_LOCK);
            this.secondLock.lock();
            try {
                out.printf(MESSAGE_TEMPLATE_SUCCESS_ACQUIRE_LOCK, currentThreadName, NAME_SECOND_LOCK);
            } finally {
                this.secondLock.unlock();
                out.printf(MESSAGE_TEMPLATE_RELEASE_LOCK, currentThreadName, NAME_SECOND_LOCK);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            this.firstLock.unlock();
            out.printf(MESSAGE_TEMPLATE_RELEASE_LOCK, currentThreadName, NAME_FIRST_LOCK);
        }
    }

}
