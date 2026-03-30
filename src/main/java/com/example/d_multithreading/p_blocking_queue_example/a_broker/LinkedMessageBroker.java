package com.example.d_multithreading.p_blocking_queue_example.a_broker;

import java.util.concurrent.LinkedBlockingDeque;

public final class LinkedMessageBroker<T> extends MessageBroker<T> {
    public LinkedMessageBroker(final int capacity) {
        super(new LinkedBlockingDeque<>(capacity));
    }
}
