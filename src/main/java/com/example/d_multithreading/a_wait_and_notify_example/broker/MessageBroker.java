package com.example.d_multithreading.a_wait_and_notify_example.broker;

import com.example.d_multithreading.a_wait_and_notify_example.consumer.MessageConsumingTask;
import com.example.d_multithreading.a_wait_and_notify_example.model.Message;
import com.example.d_multithreading.a_wait_and_notify_example.producer.MessageProducingTask;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.Optional.*;

public final class MessageBroker {
    private static final String MESSAGE_OF_MESSAGE_IS_PRODUCED = "Message '%s' is produced by producer '%s'. "
            + "Amount of messages before producing: %d\n";
    private static final String MESSAGE_OF_MESSAGE_IS_CONSUMED = "Message '%s' is consumed by consumer '%s'. "
            + "Amount of messages before consuming: %d\n";

    private final Queue<Message> messagesToBeConsumed;
    private final int maxStoredMessages;

    public MessageBroker(final int maxStoredMessages) {
        this.messagesToBeConsumed = new ArrayDeque<>(maxStoredMessages);
        this.maxStoredMessages = maxStoredMessages;
    }

    public synchronized void produce(final Message message, final MessageProducingTask producingTask) {
        try {
            while (!this.isShouldProduce(producingTask)) {
                super.wait();
            }
            this.messagesToBeConsumed.add(message);
            out.printf(
                    MESSAGE_OF_MESSAGE_IS_PRODUCED,
                    message,
                    producingTask.getName(),
                    this.messagesToBeConsumed.size() - 1
            );
            super.notify();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    public synchronized Optional<Message> consume(final MessageConsumingTask consumingTask) {
        try {
            while (!this.isShouldConsume(consumingTask)) {
                super.wait();
            }
            final Message consumedMessage = this.messagesToBeConsumed.poll();
            out.printf(
                    MESSAGE_OF_MESSAGE_IS_CONSUMED,
                    consumedMessage,
                    consumingTask.getName(),
                    this.messagesToBeConsumed.size() + 1
            );
            super.notify();
            return ofNullable(consumedMessage);
        } catch (InterruptedException e) {
            currentThread().interrupt();
            return empty();
        }
    }

    private boolean isShouldProduce(final MessageProducingTask producingTask) {
        return this.messagesToBeConsumed.size() < this.maxStoredMessages
                && this.messagesToBeConsumed.size() <= producingTask.getMaximalAmountMessagesToProduce();
    }


    private boolean isShouldConsume(final MessageConsumingTask consumingTask) {
        return !this.messagesToBeConsumed.isEmpty()
                && this.messagesToBeConsumed.size() >= consumingTask.getMinimalAmountMessagesToConsume();
    }
}
