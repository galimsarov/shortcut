package com.example.d_multithreading.v_proselyte_experiments.c_collections.e_copy_on_write;

import java.util.List;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ReadFromCOWArrayWorker implements Runnable {
    private final List<Integer> list;

    public ReadFromCOWArrayWorker(final List<Integer> list) {
        this.list = list;
    }
    @Override
    public void run() {
        while (!currentThread().isInterrupted()) {
            try {
                MILLISECONDS.sleep(150);
                out.println(list);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
