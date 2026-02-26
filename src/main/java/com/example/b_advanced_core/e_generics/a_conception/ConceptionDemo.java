package com.example.b_advanced_core.e_generics.a_conception;

import java.util.ArrayList;
import java.util.List;

/**
 * Generics позволяют параметризовать типы и писать переиспользуемый, но при этом типобезопасный код.
 */
public class ConceptionDemo {
    public static void main(String[] args) {
        withGenerics();
    }

    /**
     * Без дженериков (до Java 5) приходилось работать через Object и делать приведения вручную:
     */
    private static void withoutGenerics() {
        List items = new ArrayList();
        items.add("hello");
        String s = (String) items.get(0); // ручной cast
        System.out.println(s);
    }

    /**
     * Что это даёт:
     * - проверки типов на этапе компиляции;
     * - меньше ClassCastException в рантайме;
     * - более читаемые API (List<User> понятнее, чем просто List).
     * Важно: в Java дженерики реализованы через type erasure — информация о параметрах типа в рантайме стирается.
     */
    private static void withGenerics() {
        List<String> items = new ArrayList<>();
        items.add("hello");
        String s = items.get(0); // cast не нужен
        System.out.println(s);
    }
}
