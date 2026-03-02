package com.example.c_advanced_core.e_generics.b_typed.a_class;

/**
 * Простой обобщённый контейнер:
 * @param <T>
 */
public class ABox<T> {
    private T value;

    public ABox(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
