package com.example.c_advanced_core.b_iterable;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Задача: Сделать свой класс IntRange, который можно использовать в for-each.

 * Требования:

 * Класс реализует Iterable<Integer>.
 * Поддерживает параметры: start, end, step.
 * Итерация идёт от start до end (включительно или исключительно — зафиксировать и документировать).
 * Добавить проверку на некорректный step (например, 0).
 */
public class DIntRange implements Iterable<Integer> {
    private final int start;
    private final int end;
    private final int step;
    private final boolean isEndInclusive;

    public DIntRange(int start, int end, int step, boolean isEndInclusive) {
        this.start = start;
        this.end = end;
        if (step == 0) {
            throw new IllegalArgumentException("Step cannot be 0");
        }
        this.step = step;
        this.isEndInclusive = isEndInclusive;
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {
            private int current = start;

            @Override
            public boolean hasNext() {
                if (step > 0) {
                    return isEndInclusive ? current <= end : current < end;
                } else {
                    return isEndInclusive ? current >= end : current > end;
                }
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int value = current;
                current += step;
                return value;
            }
        };
    }

    public static void main(String[] args) {
        DIntRange intRange1 = new DIntRange(3, 7, 1, true);
        print(intRange1);
        DIntRange intRange2 = new DIntRange(3, 7, 2, false);
        print(intRange2);
        DIntRange intRange3 = new DIntRange(10, 0, -1, true);
        print(intRange3);
        DIntRange intRange4 = new DIntRange(10, 0, -2, false);
        print(intRange4);
        DIntRange intRange5 = new DIntRange(3, 7, 0, false);
        print(intRange5);
    }

    private static void print(DIntRange intRange) {
        for (int x : intRange) {
            System.out.print(x + " ");
        }
        System.out.println();
    }
}
