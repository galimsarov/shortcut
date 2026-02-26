package com.example.b_advanced_core.d_exceptions.a_suppressed;

public class BrokenResource implements AutoCloseable {
    @Override
    public void close() {
        throw new RuntimeException("Exception in close()");
    }

    void doWork() {
        throw new RuntimeException("Exception in doWork()");
    }
}
