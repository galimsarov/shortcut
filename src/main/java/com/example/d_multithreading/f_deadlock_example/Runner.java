package com.example.d_multithreading.f_deadlock_example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Runner {
    public static void main(String[] args) {
        final Lock firstGivenLock = new ReentrantLock();
        final Lock secondGivenLock = new ReentrantLock();

        final Thread firstGivenThread = new Thread(new Task(firstGivenLock, secondGivenLock));
        final Thread secondGivenThread = new Thread(new Task(firstGivenLock, secondGivenLock));

        firstGivenThread.start();
        secondGivenThread.start();
    }
}
