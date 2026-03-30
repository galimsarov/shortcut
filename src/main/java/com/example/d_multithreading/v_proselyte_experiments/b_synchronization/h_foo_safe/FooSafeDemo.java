package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.h_foo_safe;

import static java.lang.Thread.currentThread;

public class FooSafeDemo {
    public static void main(String[] args) {
        FooSafe foo = new FooSafe();
        Thread t1 = new Thread(foo::first);
        Thread t2 = new Thread(foo::second);
        Thread t3 = new Thread(foo::third);

        t1.setName("T1");
        t2.setName("T2");
        t3.setName("T3");

        t1.start();
        t2.start();
        t3.start();

        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
