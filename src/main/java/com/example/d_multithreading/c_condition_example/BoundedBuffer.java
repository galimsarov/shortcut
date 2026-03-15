package com.example.d_multithreading.c_condition_example;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;

public final class BoundedBuffer<T> {
    private final T[] elements;
    private final Lock lock;
    private final Condition notFull;
    private final Condition notEmpty;

    private int size;

    @SuppressWarnings("unchecked")
    public BoundedBuffer(int capacity) {
        this.elements = (T[]) new Object[capacity];
        this.lock = new ReentrantLock();
        this.notFull = lock.newCondition();
        this.notEmpty = lock.newCondition();
    }

    public boolean isFull() {
        this.lock.lock();
        try {
            return this.size == this.elements.length;
        } finally {
            this.lock.unlock();
        }
    }

    public boolean isEmpty() {
        this.lock.lock();
        try {
            return this.size == 0;
        } finally {
            this.lock.unlock();
        }
    }

    public void put(final T element) {
        this.lock.lock();
        try {
            while (this.isFull()) {
                this.notFull.await();
            }
            this.elements[this.size] = element;
            this.size++;
            out.printf("%s was put in buffer. Result buffer: %s%n", element, this);
            this.notEmpty.signal();
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        } finally {
            this.lock.unlock();
        }
    }

    public T take() {
        this.lock.lock();
        try {
            while (this.isEmpty()) {
                this.notEmpty.await();
            }
            final T result = this.elements[this.size - 1];
            this.elements[this.size - 1] = null;
            this.size--;
            out.printf("%s was taken from buffer. Result buffer: %s%n", result, this);
            this.notFull.signal();
            return result;
        } catch (final InterruptedException e) {
            currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            this.lock.unlock();
        }
    }

    @Override
    public String toString() {
        this.lock.lock();
        try {
            return "{"
                    + stream(this.elements, 0, this.size)
                    .map(Object::toString)
                    .collect(Collectors.joining(","))
                    + "}";
        } finally {
            this.lock.unlock();
        }
    }
}
