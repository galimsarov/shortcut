package com.example.a_alg_and_structs.d_modern_java.a_apple_example;

import com.example.a_alg_and_structs.d_modern_java.a_apple_example.pretty_printer.AppleFancyFormatter;

@FunctionalInterface
public interface DumbFunctionalInterface {
    String accept(Apple apple);

//    boolean transform(Apple apple);
    
    default String accept(AppleFancyFormatter formatter) {
        return "smth;";
    }
}
