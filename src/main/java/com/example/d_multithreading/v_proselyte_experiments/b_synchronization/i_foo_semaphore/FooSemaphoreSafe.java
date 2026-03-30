package com.example.d_multithreading.v_proselyte_experiments.b_synchronization.i_foo_semaphore;

import java.util.concurrent.Semaphore;

import static java.lang.System.out;
import static java.lang.Thread.currentThread;

public class FooSemaphoreSafe {
    private final Semaphore betweenFirstAndSecond = new Semaphore(0);
    private final Semaphore betweenSecondAndThird = new Semaphore(0);

    public void first() {
        out.println("first");
        this.betweenFirstAndSecond.release();
    }

    public void second() {
        try {
            this.betweenFirstAndSecond.acquire();
            out.println("second");
            this.betweenSecondAndThird.release();
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }

    public void third() {
        try {
            this.betweenSecondAndThird.acquire();
            out.println("third");
        } catch (InterruptedException e) {
            currentThread().interrupt();
        }
    }
}
