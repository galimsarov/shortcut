package com.example.d_multithreading.p_blocking_queue_example.b_task;

import com.example.d_multithreading.p_blocking_queue_example.a_broker.MessageBroker;

import java.util.function.Supplier;

import static java.lang.System.out;

public final class MessageBrokerProducingTask<T> extends MessageBrokerTask<T> {
    private final Supplier<T> messageSupplier;

    public MessageBrokerProducingTask(
            final MessageBroker<T> broker,
            final long secondTimeout,
            final Supplier<T> messageSupplier
    ) {
        super(broker, secondTimeout);
        this.messageSupplier = messageSupplier;
    }


    @Override
    protected void executeOperation(final MessageBroker<T> broker) throws InterruptedException {
        final T message = messageSupplier.get();
        broker.put(message);
        out.println(message + " was produced");
    }
}
