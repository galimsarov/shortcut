package com.example.d_multithreading.j_count_down_latch_example;

import static java.util.Arrays.stream;

public final class ThreadUtil {
    public static void startThreads(final Thread[] threads) {
        stream(threads).forEach(Thread::start);
    }

    private ThreadUtil() {
        throw new UnsupportedOperationException();
    }
}
