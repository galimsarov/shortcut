package com.example.c_advanced_core.e_generics.g_tasks.a_universal_box;

public class Box<T> {
    private T value;

    public void set(T value) {
        this.value = value;
    }

    public T get() {
        T valueForReturn = value;
        value = null;
        return valueForReturn;
    }
}
