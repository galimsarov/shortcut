package com.example.d_multithreading.v_proselyte_experiments.a_basic.e_thread_counter_join;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class ThreadCounterJoinDemo {
    public static void main(String[] args) {
        ThreadCounterWorker tcw1 = new ThreadCounterWorker("A", 15);
        ThreadCounterWorker tcw2 = new ThreadCounterWorker("B", 1000);

        tcw1.start();
        tcw2.start();

        try {
            tcw1.join();
            tcw2.join();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }

        out.println("Process is finished!!!");
    }
}
