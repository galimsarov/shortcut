package com.example.b_advanced_core.f_stream_api.c_lambda;

import java.util.Comparator;

/**
 * Лямбда — это не просто более короткая запись анонимного класса.
 * Она не создаёт собственного класса, имеет другой контекст this, компилируется через invokedynamic,
 * лучше оптимизируется JVM и предназначена для реализации функциональных интерфейсов.
 */
public class LambdaDemo {
    public static void main(String[] args) {
        Comparator<String> byLength = BY_LAMBDA;
        System.out.println(byLength.compare("cat", "elephant"));
    }

    /**
     * С Java 8 тот же код компактнее лямбдой:
     */
    private static final Comparator<String> BY_LAMBDA = (a, b) -> Integer.compare(a.length(), b.length());

    /**
     * И ещё компактнее через метод-референс + фабрику:
     */
    private static final Comparator<String> BY_METHOD_REFERENCE = Comparator.comparingInt(String::length);
}