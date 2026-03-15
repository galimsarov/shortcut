package com.example.d_multithreading.i_semaphor_example;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class ConnectionPoolTask extends AbstractPoolTask<Connection> {
    public ConnectionPoolTask(final AbstractPool<Connection> pool) {
        super(pool);
    }

    @Override
    protected void handle(final Connection connection) {
        try {
            connection.setAutoCommit(false);
            SECONDS.sleep(3);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
