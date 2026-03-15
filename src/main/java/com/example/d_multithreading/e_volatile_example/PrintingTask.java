package com.example.d_multithreading.e_volatile_example;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class PrintingTask implements Runnable {
    private volatile boolean shouldPrint = true;

    public void setShouldPrint(final boolean shouldPrint) {
        this.shouldPrint = shouldPrint;
    }

    @Override
    public void run() {
        try {
            while (this.shouldPrint) {
                out.println("I am working");
                MILLISECONDS.sleep(100);
            }
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
