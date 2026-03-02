package com.example.c_advanced_core.a_collections.d_tasks;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 1.2 Средний: LRU-кэш последних запросов
 * Задача: Сделать кэш фиксированного размера для строковых запросов (например, URL).

 * Требования:

 * Использовать LinkedHashMap с режимом access-order.
 * Ограничить размер кэша (например, 5 элементов).
 * При переполнении удалять самый старый по использованию элемент.
 * Реализовать методы:
 * put(String key, String value)
 * get(String key)
 * printState()
 * Показать в main, как меняется порядок элементов после get.
 */

public class BLRUCache {
    private static final LinkedHashMap<String, String> LRU =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
                    return size() > 5;
                }
            };

    private static void put(String key, String value) {
        LRU.put(key, value);
    }

    private static String get(String key) {
        return LRU.get(key);
    }

    private static void printState() {
        System.out.println(LRU);
    }

    public static void main(String[] args) {
        put("key1", "value1");
        put("key2", "value2");
        put("key3", "value3");
        put("key4", "value4");
        put("key5", "value5");
        printState();

        put("key6", "value6");
        printState();

        get("key2");
        printState();
    }
}
