package com.example.d_multithreading.p_blocking_queue_example;

import com.example.d_multithreading.p_blocking_queue_example.a_broker.MessageBroker;
import com.example.d_multithreading.p_blocking_queue_example.a_broker.SynchronousMessageBroker;
import com.example.d_multithreading.p_blocking_queue_example.b_task.MessageBrokerConsumingTask;
import com.example.d_multithreading.p_blocking_queue_example.b_task.MessageBrokerProducingTask;

import static java.util.concurrent.ThreadLocalRandom.current;

public class Runner {
    public static void main(String[] args) {
        final MessageBroker<Integer> broker = new SynchronousMessageBroker<>();

        startProducing(broker, 1);
        startProducing(broker, 3);
        startProducing(broker, 5);

        startConsuming(broker, 5);
        startConsuming(broker, 3);
        startConsuming(broker, 1);
    }

    private static void startProducing(final MessageBroker<Integer> broker, final long secondTimeout) {
        new Thread(new MessageBrokerProducingTask<>(broker, secondTimeout, Runner::generateInt)).start();
    }

    private static void startConsuming(final MessageBroker<Integer> broker, final long secondTimeout) {
        new Thread(new MessageBrokerConsumingTask<>(broker, secondTimeout)).start();
    }

    private static int generateInt() {
        return current().nextInt(0, 10);
    }
}
