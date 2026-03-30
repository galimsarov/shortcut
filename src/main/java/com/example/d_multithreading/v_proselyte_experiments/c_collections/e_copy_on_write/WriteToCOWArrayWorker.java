package com.example.d_multithreading.v_proselyte_experiments.c_collections.e_copy_on_write;

import java.util.List;
import java.util.Random;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class WriteToCOWArrayWorker implements Runnable {
    private final List<Integer> list;
    private final Random random;

    public WriteToCOWArrayWorker(final List<Integer> list) {
        this.list = list;
        random = new Random();
    }

    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                MILLISECONDS.sleep(100);
                this.list.set(this.random.nextInt(this.list.size()), this.random.nextInt());
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
