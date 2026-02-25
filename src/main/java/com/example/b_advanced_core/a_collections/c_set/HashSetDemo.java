package com.example.b_advanced_core.a_collections.c_set;

import java.util.HashSet;
import java.util.Set;

public class HashSetDemo {
    public static void main(String[] args) {
        Set<String> tags = new HashSet<>();
        tags.add("java");
        tags.add("collections");
        tags.add("java"); // дубликат не добавится

        System.out.println(tags); // порядок произвольный
        System.out.println(tags.contains("java")); // обычно O(1)
    }
}
