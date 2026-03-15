package com.example.d_multithreading.i_semaphor_example;

import static java.lang.System.out;

public abstract class AbstractPoolTask<T> implements Runnable {
    private final AbstractPool<T> pool;

    protected AbstractPoolTask(AbstractPool<T> pool) {
        this.pool = pool;
    }

    @Override
    public void run() {
        final T object = this.pool.acquire();
        try {
            out.printf("%s was acquire%n", object);
            this.handle(object);
        } finally {
            out.printf("%s is being released%n", object);
            this.pool.release(object);
        }
    }

    protected abstract void handle(final T object);
}
