package com.example.d_multithreading.p_blocking_queue_example.b_task;

import com.example.d_multithreading.p_blocking_queue_example.a_broker.MessageBroker;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class MessageBrokerTask<T> implements Runnable {
    private final MessageBroker<T> broker;
    private final long secondTimeout;

    protected MessageBrokerTask(final MessageBroker<T> broker, final long secondTimeout) {
        this.broker = broker;
        this.secondTimeout = secondTimeout;
    }

    @Override
    public final void run() {
        try {
            while (true) {
                executeOperation(broker);
                SECONDS.sleep(secondTimeout);
            }
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }

    protected abstract void executeOperation(final MessageBroker<T> broker) throws InterruptedException;
}
