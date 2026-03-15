package com.example.d_multithreading.j_count_down_latch_example;

import java.util.concurrent.CountDownLatch;

public abstract class ResourceTaskFactory {
    private long nextId;

    public final ResourceTask create(final CountDownLatch latch) {
        return this.create(this.nextId++, latch);
    }

    protected abstract ResourceTask create(final long id, final CountDownLatch latch);
}
