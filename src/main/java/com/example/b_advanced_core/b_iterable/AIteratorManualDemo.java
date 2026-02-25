package com.example.b_advanced_core.b_iterable;

import java.util.Iterator;
import java.util.List;

public class AIteratorManualDemo {
    public static void main(String[] args) {
        List<String> names = List.of("Alice", "Bob", "Charlie");
        Iterator<String> it = names.iterator();

        while (it.hasNext()) {
            String name = it.next();
            System.out.println(name);
        }
    }
}
