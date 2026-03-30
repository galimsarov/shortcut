package com.example.d_multithreading.v_proselyte_experiments.c_collections.f_concurent_hash_map;

import java.util.Map;

import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class WriteToCHMWorker implements Runnable {
    private final Map<String, Integer> map;

    public WriteToCHMWorker(final Map<String, Integer> map) {
        this.map = map;
    }
    @Override
    public void run() {
        try {
            map.put("A", 1);
            MILLISECONDS.sleep(100);
            map.put("B", 2);
            MILLISECONDS.sleep(100);
            map.put("C", 3);
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
