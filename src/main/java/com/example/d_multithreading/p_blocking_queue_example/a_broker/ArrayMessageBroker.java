package com.example.d_multithreading.p_blocking_queue_example.a_broker;

import java.util.concurrent.ArrayBlockingQueue;

public final class ArrayMessageBroker<T> extends MessageBroker<T> {
    public ArrayMessageBroker(final int capacity) {
        super(new ArrayBlockingQueue<>(capacity));
    }
}
