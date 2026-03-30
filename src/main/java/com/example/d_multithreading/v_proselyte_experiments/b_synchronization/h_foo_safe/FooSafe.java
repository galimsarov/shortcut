package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.h_foo_safe;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class FooSafe {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition secondMethodCalled = lock.newCondition();
    private final Condition thirdMethodCalled = lock.newCondition();

    private boolean isFirstMethodCalled = false;
    private boolean isSecondMethodCalled = false;

    public void first() {
        this.lock.lock();
        try {
            out.println("first");
            isFirstMethodCalled = true;
            this.secondMethodCalled.signal();
        } finally {
            this.lock.unlock();
        }
    }

    public void second() {
        this.lock.lock();
        try {
            while (!isFirstMethodCalled) {
                this.secondMethodCalled.await();
            }
            out.println("second");
            isSecondMethodCalled = true;
            this.thirdMethodCalled.signal();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            this.lock.unlock();
        }
    }

    public void third() {
        this.lock.lock();
        try {
            while (!isSecondMethodCalled) {
                this.thirdMethodCalled.await();
            }
            out.println("third");
        } catch (InterruptedException e) {
            currentThread().interrupt();
        } finally {
            this.lock.unlock();
        }
    }
}
