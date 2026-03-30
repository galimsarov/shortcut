package com.example.d_multithreading.v_proselyte_experiments.c_collections.d_priority_queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class PriorityQueueDemo {
    public static void main(String[] args) {
        BlockingQueue<Integer> queue = new PriorityBlockingQueue<>();

        WriteToPriorityQueueWorker writeToPriorityQueueWorker = new WriteToPriorityQueueWorker(queue);
        ReadFromPriorityQueueWorker readFromPriorityQueueWorker = new ReadFromPriorityQueueWorker(queue);

        Thread t1 = new Thread(writeToPriorityQueueWorker);
        Thread t2 = new Thread(readFromPriorityQueueWorker);

        t1.start();
        t2.start();
    }
}
