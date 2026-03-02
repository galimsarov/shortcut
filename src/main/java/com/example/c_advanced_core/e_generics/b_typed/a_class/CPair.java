package com.example.c_advanced_core.e_generics.b_typed.a_class;

/**
 * Можно использовать несколько параметров типа:
 * @param <K>
 * @param <V>
 */

public class CPair<K, V> {
    private final K key;
    private final V value;

    public CPair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() { return key; }
    public V getValue() { return value; }
}