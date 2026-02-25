package com.example.a_core.e_abstractions.c_combining;

/**
 * Комбинирование вместе (частый паттерн)
 * Это очень распространено:
 * - Абстрактный класс даёт общую базу.
 * - Интерфейсы добавляют роли.
 */
class Cat extends Animal implements Pet {
    @Override
    public void makeSound() {
        System.out.println("Meow");
    }

    @Override
    public void play() {
        System.out.println("The cat is playing");
    }
}
