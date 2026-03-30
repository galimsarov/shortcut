package com.example.d_multithreading.n_synchronized_collections;

import java.util.List;
import java.util.Vector;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class BVectorExample {
    public static void main(String[] args) throws InterruptedException {
        final List<Integer> values =new Vector<>();
        values.add(1);
        values.add(2);
        values.add(3);

        Runnable task = () -> addIfNotExist(values, 4);
        final Thread firstThread = new Thread(task);
        final Thread secondThread = new Thread(task);

        firstThread.start();
        secondThread.start();

        firstThread.join();
        secondThread.join();

        out.println(values);
    }

    private static void addIfNotExist(List<Integer> values, final Integer element) {
        try {
            if (!values.contains(element)) {
                MILLISECONDS.sleep(100);
                values.add(element);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
