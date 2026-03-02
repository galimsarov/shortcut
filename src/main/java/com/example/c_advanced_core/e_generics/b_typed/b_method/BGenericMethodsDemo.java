package com.example.c_advanced_core.e_generics.b_typed.b_method;

import java.util.List;

public class BGenericMethodsDemo {
    public static void main(String[] args) {
        AGenericMethods.printArray(new String[]{"A", "B"});
        AGenericMethods.printArray(new Integer[]{1, 2, 3});

        String first = AGenericMethods.firstOrNull(List.of("x", "y"));
        System.out.println("first = " + first);
        Integer n = AGenericMethods.firstOrNull(List.of(10, 20));
        System.out.println("n = " + n);

        System.out.println(0.1 + 0.2);
    }
}
