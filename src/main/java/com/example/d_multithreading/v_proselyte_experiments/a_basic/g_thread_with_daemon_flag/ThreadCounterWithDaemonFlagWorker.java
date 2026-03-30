package com.example.d_multithreading.v_proselyte_experiments.a_basic.g_thread_with_daemon_flag;

import static java.lang.System.out;

public class ThreadCounterWithDaemonFlagWorker extends Thread {
    private final String name;
    private final Integer range;

    public ThreadCounterWithDaemonFlagWorker(final String name, final Integer range, final boolean isDaemon) {
        this.name = name;
        this.range = range;
        super.setDaemon(isDaemon);
    }

    @Override
    public void run() {
        int counter = 0;
        while (counter <= this.range) {
            out.println(this.name + ": " + counter++);
        }
        out.println(this.name + " COUNTER FINISHED THE WORK");
    }
}
