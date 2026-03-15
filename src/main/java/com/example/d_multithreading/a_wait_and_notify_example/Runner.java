package com.example.d_multithreading.a_wait_and_notify_example;

import com.example.d_multithreading.a_wait_and_notify_example.broker.MessageBroker;
import com.example.d_multithreading.a_wait_and_notify_example.consumer.MessageConsumingTask;
import com.example.d_multithreading.a_wait_and_notify_example.producer.MessageFactory;
import com.example.d_multithreading.a_wait_and_notify_example.producer.MessageProducingTask;

import java.util.Arrays;

public class Runner {
    public static void main(String[] args) {
        final int brokerMaxStoredMessages = 15;
        final MessageBroker messageBroker = new MessageBroker(brokerMaxStoredMessages);

        final MessageFactory messageFactory = new MessageFactory();

        final Thread firstProducingThread = new Thread(new MessageProducingTask(
                messageBroker, messageFactory, brokerMaxStoredMessages, "PRODUCER_1"
        ));
        final Thread secondProducingThread = new Thread(new MessageProducingTask(
                messageBroker, messageFactory, 10, "PRODUCER_2"
        ));
        final Thread thirdProducingThread = new Thread(new MessageProducingTask(
                messageBroker, messageFactory, 5, "PRODUCER_3"
        ));

        final Thread firstConsumingThread = new Thread(new MessageConsumingTask(
                messageBroker, 0, "CONSUMER_1"
        ));
        final Thread secondConsumingThread = new Thread(new MessageConsumingTask(
                messageBroker, 6, "CONSUMER_2"
        ));
        final Thread thirdConsumingThread = new Thread(new MessageConsumingTask(
                messageBroker, 11, "CONSUMER_3"
        ));

        startThreads(
                firstProducingThread,
                secondProducingThread,
                thirdProducingThread,
                firstConsumingThread,
                secondConsumingThread,
                thirdConsumingThread
        );
    }

    private static void startThreads(final Thread... threads) {
        Arrays.stream(threads).forEach(Thread::start);
    }
}
