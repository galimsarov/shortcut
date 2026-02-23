package com.example.core.e_abstractions.a_interface;

public interface Swimmable {
    public static final int INT_CONST = 1; // Поля в интерфейсе — только public static final (константы).

    void swim(); // абстрактные (по умолчанию),

    default void defaultMethod() { // default (с реализацией),
        System.out.println("I am default method");
        System.out.println("I can use private method");
        System.out.println(privateMethod());
    }

    static void staticMethod() { // Могут быть static
        System.out.println("I am static method");
    }

    private String privateMethod() { // Могут быть private
        return "I am private method";
    }
}
