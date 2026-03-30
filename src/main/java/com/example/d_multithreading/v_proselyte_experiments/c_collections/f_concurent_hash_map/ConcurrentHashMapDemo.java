package com.example.d_multithreading.v_proselyte_experiments.c_collections.f_concurent_hash_map;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapDemo {
    public static void main(String[] args) {
        Map<String, Integer> concurrentMap = new ConcurrentHashMap<>();

        WriteToCHMWorker writeToCHMWorker = new WriteToCHMWorker(concurrentMap);
        ReadFromCHMWorker readFromCHMWorker = new ReadFromCHMWorker(concurrentMap);

        Thread t1 = new Thread(writeToCHMWorker);
        Thread t2 = new Thread(writeToCHMWorker);
        Thread t3 = new Thread(readFromCHMWorker);

        t1.start();
        t2.start();
        t3.start();
    }
}
