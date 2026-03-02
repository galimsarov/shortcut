package com.example.c_advanced_core.e_generics.g_tasks.a_universal_box;

public class Cat {
    private final String name;

    public Cat(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Cat{" +
                "name='" + name + '\'' +
                '}';
    }
}
