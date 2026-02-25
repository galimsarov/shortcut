package com.example.a_core.c_runtime.e_memory_leak;

class Person {
    String name; // Поле хранится в куче вместе с объектом

    Person(String name) {
        this.name = name;
    }

    void sayHello() {
        System.out.println("Hello, " + name);
    }
}
