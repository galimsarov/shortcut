package com.example.b_advanced_core.e_generics.f_pecs;

import java.util.ArrayList;
import java.util.List;

/**
 * Правило PECS:
 * - Producer Extends (? extends T) — когда источник производит значения типа T (мы читаем).
 * - Consumer Super (? super T) — когда приёмник потребляет значения типа T (мы пишем).
 * Классический пример копирования:
 */
public class PECSDemo {
    public static <T> void copy(List<? extends T> src, List<? super T> dst) {
        for (T item : src) {
            dst.add(item);
        }
    }

    /**
     * src — producer (читаем, поэтому extends), dst — consumer (пишем, поэтому super).
     * @param args
     */
    public static void main(String[] args) {
        List<Integer> src = List.of(1, 2, 3);
        List<Number> dst = new ArrayList<>();
        copy(src, dst); // ок: Integer -> Number
        System.out.println(dst);
    }
}
