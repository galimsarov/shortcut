package com.example.d_multithreading.n_synchronized_collections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static java.lang.System.out;

public class ASynchronizedListExample {
    public static void main(String[] args) throws InterruptedException {
        final List<Integer> values = Collections.synchronizedList(new ArrayList<>());
        final Runnable task = () -> IntStream.range(0, 1000).forEach(values::add);

        final Thread firstThread = new Thread(task);
        final Thread secondThread = new Thread(task);

        firstThread.start();
        secondThread.start();

        firstThread.join();
        secondThread.join();

        out.println(values.size());
    }
}
