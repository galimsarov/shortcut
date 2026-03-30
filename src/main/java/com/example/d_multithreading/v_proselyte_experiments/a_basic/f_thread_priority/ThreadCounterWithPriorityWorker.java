package com.example.d_multithreading.v_proselyte_experiments.a_basic.f_thread_priority;

import static java.lang.System.out;

public class ThreadCounterWithPriorityWorker extends Thread {
    private final String name;
    private final Integer range;

    public ThreadCounterWithPriorityWorker(final String name, final Integer range, final Integer priority) {
        this.name = name;
        this.range = range;
        super.setPriority(priority);
    }

    @Override
    public void run() {
        int counter = 0;
        while (counter <= this.range) {
            out.println(this.name + ": " + counter++);
        }
    }
}
