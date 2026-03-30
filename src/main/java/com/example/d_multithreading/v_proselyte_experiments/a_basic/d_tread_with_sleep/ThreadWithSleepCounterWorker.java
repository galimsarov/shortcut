package com.example.d_multithreading.v_proselyte_experiments.a_basic.d_tread_with_sleep;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ThreadWithSleepCounterWorker extends Thread {
    private final String name;
    private final Integer range;

    public ThreadWithSleepCounterWorker(final String name, final Integer range) {
        this.name = name;
        this.range = range;
    }

    @Override
    public void run() {
        int counter = 0;
        while (counter <= this.range) {
            out.println(this.name + ": " + counter++);
            try {
                SECONDS.sleep(1);
            } catch (InterruptedException e) {
                currentThread().interrupt();
            }
        }
    }
}
