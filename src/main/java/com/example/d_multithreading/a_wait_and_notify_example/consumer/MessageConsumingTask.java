package com.example.d_multithreading.a_wait_and_notify_example.consumer;

import com.example.d_multithreading.a_wait_and_notify_example.broker.MessageBroker;
import com.example.d_multithreading.a_wait_and_notify_example.model.Message;

import java.util.Optional;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MessageConsumingTask implements Runnable {
    private static final int SECONDS_DURATION_TO_SLEEP_BEFORE_CONSUMING = 3;

    private final MessageBroker messageBroker;
    private final int minimalAmountMessagesToConsume;
    private final String name;

    public MessageConsumingTask(
            final MessageBroker messageBroker,
            final int minimalAmountMessagesToConsume,
            final String name
    ) {
        this.messageBroker = messageBroker;
        this.minimalAmountMessagesToConsume = minimalAmountMessagesToConsume;
        this.name = name;
    }

    public int getMinimalAmountMessagesToConsume() {
        return this.minimalAmountMessagesToConsume;
    }

    public String getName() {
        return this.name;
    }

    @Override
    public void run() {
        try {
            while (!currentThread().isInterrupted()) {
                SECONDS.sleep(SECONDS_DURATION_TO_SLEEP_BEFORE_CONSUMING);
                final Optional<Message> optionalConsumedMessage = this.messageBroker.consume(this);
                optionalConsumedMessage.orElseThrow(MessageConsumingException::new);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
