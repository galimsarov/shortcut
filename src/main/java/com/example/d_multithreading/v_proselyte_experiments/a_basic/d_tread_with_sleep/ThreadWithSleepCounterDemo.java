package com.example.d_multithreading.v_proselyte_experiments.a_basic.d_tread_with_sleep;

public class ThreadWithSleepCounterDemo {
    public static void main(String[] args) {
        ThreadWithSleepCounterWorker tcw1 = new ThreadWithSleepCounterWorker("A", 15);
        ThreadWithSleepCounterWorker tcw2 = new ThreadWithSleepCounterWorker("B", 15);

        tcw1.start();
        tcw2.start();
    }
}
