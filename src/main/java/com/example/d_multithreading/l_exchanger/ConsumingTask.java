package com.example.d_multithreading.l_exchanger;

import java.util.Queue;
import java.util.concurrent.Exchanger;

import static java.lang.System.out;

public final class ConsumingTask extends ExchangingTask {
    public ConsumingTask(final Exchanger<Queue<ExchangedObject>> exchanger) {
        super(exchanger);
    }

    @Override
    protected void handle(final Queue<ExchangedObject> objects) {
        while (!objects.isEmpty()) {
            final ExchangedObject object = objects.poll();
            out.printf("%s was consumed%n", object);
        }
        out.println("----------------------------");
    }
}
