package com.example.b_advanced_core.f_stream_api.b_functional_interface;

/**
 * Функциональный интерфейс — интерфейс с ровно одним абстрактным методом (SAM: single abstract method).
 */
@FunctionalInterface
public interface Calculator {
    int apply(int a, int b);
}
