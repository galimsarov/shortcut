package com.example.b_advanced_core.d_exceptions;

/**
 * Unchecked exceptions (RuntimeException)
 * Компилятор не заставляет их обрабатывать.
 * Когда полезны: ошибки валидации, ошибки контракта API, неверное состояние объекта.
 */

public class UncheckedExample {
    static int divide(int a, int b) {
        if (b == 0) {
            throw new IllegalArgumentException("b must not be 0");
        }
        return a / b;
    }

    public static void main(String[] args) {
        System.out.println(divide(10, 2));
        System.out.println(divide(5, 0)); // IllegalArgumentException
    }
}
