package com.example.c_advanced_core.a_collections.b_map;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class TreeMapExample {
    public static void main(String[] args) {
        TreeMap<String, Integer> map = new TreeMap<>();
        map.put("banana", 3);
        map.put("apple", 5);
        map.put("orange", 2);
        // Ключи в байтовом порядке: apple, banana, orange
        System.out.println("Sorted keys: " + map.keySet());
        // Навигация
        System.out.println("First key: " + map.firstKey());
        System.out.println("Ceiling key of \"ball\": " + map.ceilingKey("ball"));  // banana
        // Диапазон
        SortedMap<String, Integer> sub = map.subMap("banana", true, "orange", false);
        System.out.println("Submap banana..orange (exclusive): " + sub);
        // Обход
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}
