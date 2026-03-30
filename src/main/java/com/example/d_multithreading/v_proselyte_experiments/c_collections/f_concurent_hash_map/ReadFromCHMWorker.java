package com.example.d_multithreading.v_proselyte_experiments.c_collections.f_concurent_hash_map;

import java.util.Map;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ReadFromCHMWorker implements Runnable {
    private final Map<String, Integer> map;

    public ReadFromCHMWorker(final Map<String, Integer> map) {
        this.map = map;
    }
    @Override
    public void run() {
        try {
            MILLISECONDS.sleep(50);
            out.printf("A: %d%n", map.get("A"));
            out.printf("B: %d%n", map.get("B"));
            SECONDS.sleep(5);
            out.printf("C: %d%n", map.get("C"));
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
