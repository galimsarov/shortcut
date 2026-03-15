package com.example.d_multithreading.d_reentrant_read_write_lock_example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public final class CounterGuardedByReadWriteLock extends AbstractCounter {
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Lock readLock = this.readWriteLock.readLock();
    private final Lock writeLock = this.readWriteLock.writeLock();

    @Override
    protected Lock getReadLock() {
        return this.readLock;
    }

    @Override
    protected Lock getWriteLock() {
        return this.writeLock;
    }
}
