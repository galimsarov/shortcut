package com.example.d_multithreading.v_proselyte_experiments.a_basic.e_thread_counter_join;

import static java.lang.System.out;

public class ThreadCounterWorker extends Thread {
    private final String name;
    private final Integer range;

    public ThreadCounterWorker(final String name, final Integer range) {
        this.name = name;
        this.range = range;
    }

    @Override
    public void run() {
        int counter = 0;
        while (counter <= this.range) {
            out.println(this.name + ": " + counter++);
        }
    }
}
