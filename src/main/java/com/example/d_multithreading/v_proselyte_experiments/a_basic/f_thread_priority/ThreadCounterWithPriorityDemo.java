package com.example.d_multithreading.v_proselyte_experiments.a_basic.f_thread_priority;

public class ThreadCounterWithPriorityDemo {
    public static void main(String[] args) {
        ThreadCounterWithPriorityWorker tcw1 = new ThreadCounterWithPriorityWorker("A", 5, 10);
        ThreadCounterWithPriorityWorker tcw2 = new ThreadCounterWithPriorityWorker("B", 5, 1);

        tcw1.start();
        tcw2.start();

        System.out.println("Process is finished!!!");
    }
}
