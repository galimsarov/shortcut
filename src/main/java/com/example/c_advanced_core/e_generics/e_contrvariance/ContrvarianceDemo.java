package com.example.c_advanced_core.e_generics.e_contrvariance;

import java.util.ArrayList;
import java.util.List;

public class ContrvarianceDemo {
    /**
     * Контрвариантность задаётся через wildcard ? super T.
     * @param target
     */
    public static void addDefaults(List<? super Integer> target) {
        target.add(10);
        target.add(20);
    }

    /**
     * Метод принимает List<Integer>, List<Number>, List<Object>.
     *
     * @param args
     */
    public static void main(String[] args) {
        List<Number> numbers = new ArrayList<>();
        addDefaults(numbers); // ок

    }
}
