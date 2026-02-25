package com.example.a_core.c_runtime.e_memory_leak;

public class MemoryDemo {
    public static void main(String[] args) {
        int x = 10; // Хранится в стеке
        Person p = new Person("Alice"); // 'p' находится в стеке, объект — в куче

        p.sayHello(); // Вызов метода создаёт новый стек-фрейм
    }
}
