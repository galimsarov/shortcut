package com.example.d_multithreading.v_proselyte_experiments.a_basic.a_first_example;

import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class Service {
    public void readData() {
        System.out.println("Read data");
    }

    public void showGreetingMessage() {
        System.out.println("Hello!!!");
    }

    public void calculateFactorial(int number) {
        long result = LongStream.range(2, number + 1L)
                .reduce(1, (a, b) -> a * b);
        System.out.println("Factorial result: " + result);
    }

    public void calculateSum(int number) {
        long sum = IntStream.range(1, number + 1)
                .reduce(0, (a, b) -> {
                    int res = a + b;
                    System.out.println("The current sum is: " + res);
                    return res;
                });
        System.out.println("The total sum is: " + sum);
    }

    public void finishProgram() {
        System.out.println("Finish");
        System.exit(0);
    }
}
