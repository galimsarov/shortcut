package com.example.core.d_gc.basic;

public class GcBasicExample {
    static class User {
        String name;

        User(String name) {
            this.name = name;
        }
    }

    public static void main(String[] args) {
        User u = new User("Alice");
        // объект достижим через локальную переменную u

        u = null;
        // после этой строки объект недостижим и может быть собран GC

        System.gc(); // только просьба к JVM, а не гарантия немедленного GC
    }
}
