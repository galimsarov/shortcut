package com.example.b_advanced_core.a_collections.c_set;

import java.util.TreeSet;

public class TreeSetDemo {
    public static void main(String[] args) {
        TreeSet<Integer> scores = new TreeSet<>();
        scores.add(50);
        scores.add(10);
        scores.add(30);

        System.out.println(scores);         // [10, 30, 50]
        System.out.println(scores.ceiling(25)); // 30
        System.out.println(scores.floor(25));   // 10
    }
}
