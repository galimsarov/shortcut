package com.example.d_multithreading.q_callable_example.a_number_generator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Runner {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(new NumberGeneratingTask());
        Thread thread = new Thread(futureTask);
        thread.start();
        Integer i = futureTask.get();
        System.out.println(i);
    }
}
