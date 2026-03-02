package com.example.c_advanced_core.f_stream_api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Задача: отфильтровать чётные числа и возвести в квадрат.
 */
public class CompareStylesDemo {
    public static void main(String[] args) {
        List<Integer> source = Arrays.asList(1, 2, 3, 4, 5, 6);

        // 1) Императивный стиль
        List<Integer> imperative = new ArrayList<>();
        for (Integer n : source) {
            if (n % 2 == 0) {
                imperative.add(n * n);
            }
        }

        // 2) Через Stream API + лямбды
        List<Integer> streamLambda = source.stream()
                .filter(n -> n % 2 == 0)
                .map(n -> n * n)
                .toList();

        System.out.println(imperative);   // [4, 16, 36]
        System.out.println(streamLambda); // [4, 16, 36]
    }
}
