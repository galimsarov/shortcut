package com.example.d_multithreading.v_proselyte_experiments.a_basic.c_runnable_counter;

public class RunnableCounterDemo {
    public static void main(String[] args) {
        RunnableCounterWorker scw1 = new RunnableCounterWorker("A", 15);
        RunnableCounterWorker scw2 = new RunnableCounterWorker("B", 15);

        Thread t1 = new Thread(scw1);
        Thread t2 = new Thread(scw2);
        t1.start();
        t2.start();
    }
}
