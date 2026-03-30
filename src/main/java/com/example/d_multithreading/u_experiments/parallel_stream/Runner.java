package com.example.d_multithreading.u_experiments.parallel_stream;

import java.util.stream.StreamSupport;

import static java.lang.System.out;

public class Runner {
    public static void main(String[] args) {
        final long sum = StreamSupport.longStream(
                        new LongArraySpliterator(new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}),
                        true
                ).filter(value -> {
                    out.println(Thread.currentThread().getName() + ": " + value);
                    return value % 2 == 0;
                })
                .sum();
        out.println(sum);
    }
}
