package com.example.d_multithreading.q_callable_example.b_coordinates.util;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class FutureUtil {

    public static <T> T get(final Future<T> future) {
        try {
            return future.get();
        } catch (final InterruptedException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }

    private FutureUtil() {

    }
}
