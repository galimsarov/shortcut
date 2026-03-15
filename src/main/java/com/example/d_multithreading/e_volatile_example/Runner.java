package com.example.d_multithreading.e_volatile_example;

import static java.lang.System.out;
import static java.util.concurrent.TimeUnit.SECONDS;

public class Runner {
    public static void main(String[] args) throws InterruptedException {
        final PrintingTask printingTask = new PrintingTask();
        final Thread printingThread = new Thread(printingTask);

        printingThread.start();

        SECONDS.sleep(5);

        printingTask.setShouldPrint(false);
        out.println("Printing should be stopped");
    }
}
