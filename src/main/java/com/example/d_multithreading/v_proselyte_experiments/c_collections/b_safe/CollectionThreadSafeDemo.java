package com.example.d_multithreading.v_proselyte_experiments.c_collections.b_safe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class CollectionThreadSafeDemo {
    public static void main(String[] args) {
        List<Integer> threadSafeList = Collections.synchronizedList(new ArrayList<>());
        Thread t1 = new Thread(() -> {
           for (int i = 0; i < 1_000_000; i++) {
               threadSafeList.add(i);
           }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1_000_000; i++) {
                threadSafeList.add(i);
            }
        });
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
        out.printf("threadSafeList size: %d%n", threadSafeList.size());
    }
}
