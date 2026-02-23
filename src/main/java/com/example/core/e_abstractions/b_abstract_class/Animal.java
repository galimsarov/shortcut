package com.example.core.e_abstractions.b_abstract_class;

/**
 * Абстрактный класс — это частично готовый базовый класс: может содержать и общую реализацию, и абстрактные методы.
 * Особенности абстрактных классов
 * - Можно хранить состояние (обычные поля).
 * - Можно иметь конструкторы.
 * - Можно писать любую логику методов (в т.ч. private, protected, final и т.д.).
 * - Класс может наследоваться только от одного класса (в том числе абстрактного).
 */

abstract class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void sleep() { // уже готовая логика
        System.out.println(name + " sleeps");
    }

    public abstract void makeSound(); // обязателен к реализации в наследнике
}

