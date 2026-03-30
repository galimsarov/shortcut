package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.k_deadlock;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.SECONDS;

public class DeadlockDemo {
    public static void main(String[] args) {
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();

        Thread t1 = new Thread(new SyncThread(obj1, obj2), "t1");
        Thread t2 = new Thread(new SyncThread(obj2, obj3), "t2");
        Thread t3 = new Thread(new SyncThread(obj3, obj1), "t3");

        t1.start();
        try {
            SECONDS.sleep(1);
            t2.start();
            SECONDS.sleep(1);
            t3.start();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
