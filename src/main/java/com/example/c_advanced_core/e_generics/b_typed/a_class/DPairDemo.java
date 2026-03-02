package com.example.c_advanced_core.e_generics.b_typed.a_class;

public class DPairDemo {
    public static void main(String[] args) {
        CPair<String, Integer> pair = new CPair<>("Hello", 1);
        System.out.println(pair);
        System.out.println(pair.getKey());
        System.out.println(pair.getValue());
    }
}
