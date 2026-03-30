package com.example.a_alg_and_structs.d_modern_java.a_apple_example.pretty_printer;

import com.example.a_alg_and_structs.d_modern_java.a_apple_example.Apple;

public class AppleFancyFormatter implements AppleFormatter {
    @Override
    public String accept(Apple apple) {
        String characteristic = apple.getWeight() > 150 ? "heavy" : "light";
        return "A " + characteristic +
                " " + apple.getColor() +" apple";
    }
}
