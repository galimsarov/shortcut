package com.example.b_advanced_core.a_collections.b_map.b_linked_hash_map;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Сохраняет порядок (вставки или доступа).

 * По умолчанию — порядок вставки.
 * Можно включить accessOrder=true для LRU-подобного поведения.
 */

public class LinkedHashMapLruDemo {
    public static void main(String[] args) {
        LinkedHashMap<Integer, String> lru = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<Integer, String> eldest) {
                return size() > 3;
            }
        };

        lru.put(1, "A");
        lru.put(2, "B");
        lru.put(3, "C");
        System.out.println(lru);

        lru.get(1);      // делаем 1 "самым свежим"
        System.out.println(lru);

        lru.put(4, "D"); // вытеснит 2
        System.out.println(lru); // {3=C, 1=A, 4=D}
    }
}
