package com.example.b_advanced_core.a_collections.b_map.b_linked_hash_map;

import java.util.LinkedHashMap;

public class LinkedHashMapDemo {
    public static void main(String[] args) {


    }

    private static void example2() {
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>(16, 0.8f, true);
        map.put(1, 1);
        map.put(2, 2);
        map.get(1);
        int lastKey = map.sequencedKeySet().getLast(); // 1
        System.out.println(lastKey);
    }

    private static void example1() {
        LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();
        map.put(1, 1);
        map.put(2, 2);
        int lastKey = map.sequencedKeySet().getLast(); // 2, метод есть только в LinkedHashMap
        System.out.println(lastKey);
    }
}
