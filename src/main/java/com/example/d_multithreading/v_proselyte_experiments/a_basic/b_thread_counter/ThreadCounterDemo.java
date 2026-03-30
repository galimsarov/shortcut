package com.example.d_multithreading.v_proselyte_experiments.a_basic.b_thread_counter;

public class ThreadCounterDemo {
    public static void main(String[] args) {
        ThreadCounterWorker tcw1 = new ThreadCounterWorker("A", 1000);
        ThreadCounterWorker tcw2 = new ThreadCounterWorker("B", 1000);

        tcw1.start();
        tcw2.start();
    }
}
