package com.example.b_advanced_core.f_stream_api.b_functional_interface;

import java.util.function.*;

/**
 * Стандартные функциональные интерфейсы из java.util.function:
 * - Predicate<T>: boolean test(T t) — проверка условия.
 * - Function<T, R>: R apply(T t) — преобразование.
 * - Consumer<T>: void accept(T t) — «потребление» значения (обычно side effects).
 * - Supplier<T>: T get() — поставщик значения.
 * - UnaryOperator<T> / BinaryOperator<T> — частные случаи Function.
 */
public class FunctionalInterfacesDemo {
    private static void predicateExample() {
        Predicate<String> isAlphaNumeric = s -> s.matches("^[a-zA-Z0-9]*$");
        System.out.println(isAlphaNumeric.test("abc"));
    }

    private static void functionExample() {
        Function<Integer, Integer> addTwo = x -> x + 2;
        System.out.println(addTwo.apply(5));
    }

    private static void consumerExample() {
        Consumer<String> logString = s -> System.out.println(s);
        logString.accept("Hello World");
    }

    private static void supplierExample() {
        Supplier<String> getHelloWorld = () -> "Hello World";
        System.out.println(getHelloWorld.get());
    }

    private static void unaryOperatorExample() {
        UnaryOperator<Integer> addTwo = x -> x + 2;
        System.out.println(addTwo.apply(5));
    }

    private static void binaryOperatorExample() {
        BinaryOperator<Integer> add = (x, y) -> x + y;
        System.out.println(add.apply(5, 2));
    }
}
