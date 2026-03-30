package com.example.d_multithreading.v_proselyte_experiments.a_basic.b_thread_counter;

import static java.lang.System.out;

public class ThreadCounterWorker extends Thread {
    private final String name;
    private final Integer range;

    public ThreadCounterWorker(String name, Integer range) {
        this.name = name;
        this.range = range;
    }

    @Override
    public void run() {
        int counter = 0;
        while (counter <= range) {
            out.println(name + ": " + counter++);
        }

    }
}
