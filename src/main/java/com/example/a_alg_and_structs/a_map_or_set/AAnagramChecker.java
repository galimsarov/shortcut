package com.example.a_alg_and_structs.a_map_or_set;

import java.util.HashMap;
import java.util.Map;

/**
 * Задача: даны 2 строки. Определить, являются ли эти строки анаграммами.
 * Как решал: решил сам с помощью map (подсчёт частоты символов).
 * 5 примеров тестовых данных (пары строк):
 * "listen" и "silent" → true
 * "triangle" и "integral" → true
 * "apple" и "papel" → true
 * "rat" и "car" → false
 * "aabbcc" и "abcccd" → false
 */
public class AAnagramChecker {
    public static void main(String[] args) {
        String s1 = "aabbcc", s2 = "abcccd";
        System.out.println(s1);
        System.out.println(s2);
        if (isAnagram(s1, s2)) {
            System.out.println("Anagram found");
        } else {
            System.out.println("Not Anagram found");
        }
    }

    private static boolean isAnagram(String s1, String s2) {
        if (s1.length() != s2.length()) {
            return false;
        }
        if (s1.isEmpty()) {
            return false;
        }
        Map<Character, Integer> map = new HashMap<>();
        for (int i = 0; i < s1.length(); i++) {
            char c = s1.charAt(i);
            if (map.containsKey(c)) {
                map.put(c, map.get(c) + 1);
            } else {
                map.put(c, 1);
            }
        }
        System.out.println(map);
        for (int i = 0; i < s2.length(); i++) {
            char c = s2.charAt(i);
            if (!map.containsKey(c)) {
                return false;
            }
            if (map.get(c) == 1) {
                map.remove(c);
            } else {
                map.put(c, map.get(c) - 1);
            }
        }
        return map.isEmpty();
    }
}
