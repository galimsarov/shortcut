package com.example.d_multithreading.p_blocking_queue_example.a_broker;

import java.util.concurrent.BlockingQueue;

public abstract class MessageBroker<T> {
    private final BlockingQueue<T> messages;

    protected MessageBroker(final BlockingQueue<T> messages) {
        this.messages = messages;
    }

    public final void put(final T message) throws InterruptedException {
        messages.put(message);
    }

    public final T take() throws InterruptedException {
        return messages.take();
    }
}
