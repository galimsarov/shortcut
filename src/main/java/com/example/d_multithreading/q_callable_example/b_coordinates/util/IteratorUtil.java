package com.example.d_multithreading.q_callable_example.b_coordinates.util;

import java.util.Iterator;
import java.util.List;

import static java.util.stream.StreamSupport.stream;

public final class IteratorUtil {

    public static <T> List<T> asList(final Iterator<T> iterator) {
        final Iterable<T> iterable = () -> iterator;
        return stream(iterable.spliterator(), false).toList();
    }

    private IteratorUtil() {

    }
}
