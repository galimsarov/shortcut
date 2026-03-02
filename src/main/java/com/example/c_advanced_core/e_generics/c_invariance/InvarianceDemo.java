package com.example.c_advanced_core.e_generics.c_invariance;

import java.util.List;

/**
 * В Java generic-типы инвариантны.
 * Это значит, что даже если Integer — наследник Number, List<Integer> не является подтипом List<Number>.
 * Почему так: иначе можно было бы добавить Double в список Integer и сломать типобезопасность.
 */

public class InvarianceDemo {
    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 3);
        Number number = 1;
        System.out.println(number);
//        List<Number> nums = ints; // compile error
    }
}
