package com.example.b_advanced_core.b_iterable;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Range implements Iterable<Integer> {
    private final int from;
    private final int to;

    public Range(int from, int to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = from;

            @Override
            public boolean hasNext() {
                return current <= to;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return current++;
            }
        };
    }

    public static void main(String[] args) {
        for (int x : new Range(3, 7)) {
            System.out.print(x + " "); // 3 4 5 6 7
        }
    }
}
