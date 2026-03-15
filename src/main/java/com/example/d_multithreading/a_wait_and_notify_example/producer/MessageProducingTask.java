package com.example.d_multithreading.a_wait_and_notify_example.producer;

import com.example.d_multithreading.a_wait_and_notify_example.broker.MessageBroker;
import com.example.d_multithreading.a_wait_and_notify_example.model.Message;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MessageProducingTask implements Runnable {
    private static final int SECONDS_DURATION_TO_SLEEP_BEFORE_PRODUCING = 1;

    private final MessageBroker messageBroker;
    private final MessageFactory messageFactory;
    private final int maximalAmountMessagesToProduce;
    private final String name;

    public MessageProducingTask(
            final MessageBroker messageBroker,
            final MessageFactory messageFactory,
            final int maximalAmountMessagesToProduce,
            final String name
    ) {
        this.messageBroker = messageBroker;
        this.messageFactory = messageFactory;
        this.maximalAmountMessagesToProduce = maximalAmountMessagesToProduce;
        this.name = name;
    }

    public int getMaximalAmountMessagesToProduce() {
        return this.maximalAmountMessagesToProduce;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void run() {
        try {
            while (!currentThread().isInterrupted()) {
                final Message producedMessage = this.messageFactory.create();
                SECONDS.sleep(SECONDS_DURATION_TO_SLEEP_BEFORE_PRODUCING);
                this.messageBroker.produce(producedMessage, this);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
