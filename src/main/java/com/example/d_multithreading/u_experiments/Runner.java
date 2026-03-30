package com.example.d_multithreading.u_experiments;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Runner {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Callable<Integer> task = () -> {
            System.out.println("Callable runs in: " + Thread.currentThread().getName());
            return 42;
        };
        // FutureTask<T> — это адаптер, который одновременно реализует Runnable и Future<T>:
        // его можно запустить в Thread как Runnable, а результат получить как Future.
        FutureTask<Integer> futureTask = new FutureTask<>(task);

        Thread t = new Thread(futureTask, "callable-thread");
        t.start();

        Integer result = futureTask.get(); // получаем результат (при необходимости ждём завершения)
        System.out.println("Result = " + result);
    }
}
