package com.example.b_advanced_core.a_collections.c_set;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentSetDemo {
    public static void main(String[] args) throws InterruptedException {
        Set<Integer> set = ConcurrentHashMap.newKeySet();

        ExecutorService pool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < 1000; i++) {
            int v = i % 100; // специально дубликаты
            pool.submit(() -> set.add(v));
        }

        pool.shutdown();
        pool.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println(set.size()); // ожидаемо 100
    }
}
