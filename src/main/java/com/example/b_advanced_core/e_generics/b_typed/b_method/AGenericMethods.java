package com.example.b_advanced_core.e_generics.b_typed.b_method;

import java.util.List;

/**
 * Generic-метод объявляет свои параметры типа перед возвращаемым типом:
 */
public class AGenericMethods {

    public static <T> void printArray(T[] arr) {
        for (T el : arr) {
            System.out.print(el + " ");
        }
        System.out.println();
    }

    public static <T> T firstOrNull(List<T> list) {
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Пример с ограничением (bounded type parameter):
     */
    public static <T extends Number> double sum(List<T> nums) {
        double total = 0;
        for (T n : nums) {
            total += n.doubleValue();
        }
        return total;
    }
}