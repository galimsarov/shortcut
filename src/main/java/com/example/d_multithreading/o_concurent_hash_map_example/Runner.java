package com.example.d_multithreading.o_concurent_hash_map_example;

public class Runner {
    public static void main(String[] args) {
//        CounterTestUtil.test(new SingleThreadLetterCounter());
        CounterTestUtil.test(new MultiThreadLetterCounter(5));
    }
}
