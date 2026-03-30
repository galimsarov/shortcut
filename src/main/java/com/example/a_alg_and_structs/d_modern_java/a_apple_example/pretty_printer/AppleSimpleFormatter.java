package com.example.a_alg_and_structs.d_modern_java.a_apple_example.pretty_printer;

import com.example.a_alg_and_structs.d_modern_java.a_apple_example.Apple;

public class AppleSimpleFormatter implements AppleFormatter {
    @Override
    public String accept(Apple apple) {
        return "An apple of " + apple.getWeight() + "g";
    }
}
