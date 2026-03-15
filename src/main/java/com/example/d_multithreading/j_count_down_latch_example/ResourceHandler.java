package com.example.d_multithreading.j_count_down_latch_example;

import java.util.concurrent.CountDownLatch;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public final class ResourceHandler extends ResourceTask {
    public ResourceHandler(
            final long id,
            final CountDownLatch latch
    ) {
        super(id, latch);
    }

    @Override
    public void run(final CountDownLatch latch) {
        try {
            latch.await();
            out.printf("Resources were handled by %s%n", this);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
