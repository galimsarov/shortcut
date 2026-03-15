package com.example.d_multithreading.l_exchanger;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Exchanger;

import static java.lang.Thread.currentThread;

public abstract class ExchangingTask implements Runnable {
    private final Exchanger<Queue<ExchangedObject>> exchanger;
    private Queue<ExchangedObject> objects;

    protected ExchangingTask(final Exchanger<Queue<ExchangedObject>> exchanger) {
        this.exchanger = exchanger;
        this.objects = new ArrayDeque<>();
    }

    @Override
    public final void run() {
        while (!currentThread().isInterrupted()) {
            this.handle(this.objects);
            this.exchange();
        }
    }

    protected abstract void handle(final Queue<ExchangedObject> objects);

    private void exchange() {
        try {
            this.objects = this.exchanger.exchange(this.objects);
        } catch (final InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
