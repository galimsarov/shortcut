package com.example.d_multithreading.p_blocking_queue_example.a_broker;

import java.util.concurrent.SynchronousQueue;

public final class SynchronousMessageBroker<T> extends MessageBroker<T> {
    public SynchronousMessageBroker() {
        super(new SynchronousQueue<>());
    }
}
