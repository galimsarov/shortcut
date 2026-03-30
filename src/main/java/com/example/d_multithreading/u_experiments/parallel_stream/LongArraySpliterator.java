package com.example.d_multithreading.u_experiments.parallel_stream;

import java.util.Spliterator;
import java.util.function.LongConsumer;

import static java.lang.System.out;

public class LongArraySpliterator implements Spliterator.OfLong {
    private static int spliteratorIndex = 0;

    private static final int SPLIT_THRESHOLD = 4;

    private final long[] source;
    private final int endIndex;
    private int currentIndex;

    public LongArraySpliterator(final long[] source) {
        this(source, source.length, 0);
    }

    private LongArraySpliterator(final long[] source, final int endIndex, final int currentIndex) {
        this.source = source;
        this.endIndex = endIndex;
        this.currentIndex = currentIndex;
        spliteratorIndex++;
        out.println("Created LongArraySpliterator with index: " + spliteratorIndex);
    }

    @Override
    public Spliterator.OfLong trySplit() {
        if (remainingElements() < SPLIT_THRESHOLD) {
            return null;
        }
        final int middle = currentIndex + remainingElements() / 2;
        final LongArraySpliterator prefix = new LongArraySpliterator(source, middle, currentIndex);
        currentIndex = middle;
        return prefix;
    }

    @Override
    public boolean tryAdvance(final LongConsumer action) {
        if (currentIndex < endIndex) {
            action.accept(source[currentIndex++]);
            return true;
        }
        return false;
    }

    @Override
    public int characteristics() {
        return ORDERED | NONNULL | SIZED | SUBSIZED;
    }

    @Override
    public long estimateSize() {
        return remainingElements();
    }

    private int remainingElements() {
        return endIndex - currentIndex;
    }
}
