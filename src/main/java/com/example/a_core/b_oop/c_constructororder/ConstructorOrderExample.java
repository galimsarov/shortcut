package com.example.a_core.b_oop.c_constructororder;

/**
 * Пакет 3: порядок инициализации при наследовании.
 *
 * Для создания объекта Child в Java порядок такой:
 * 1) Статические блоки родителя (один раз при первой загрузке класса).
 * 2) Статические блоки потомка (один раз при первой загрузке класса).
 * 3) Инициализация экземпляра родителя + конструктор родителя.
 * 4) Инициализация экземпляра потомка + конструктор потомка.
 */
public final class ConstructorOrderExample {

    private ConstructorOrderExample() {
    }

    public static void demo() {
        System.out.println("Creating first object Child:");
        new Child();

        System.out.println("\nCreating second object Child:");
        new Child();
    }

    static class Parent {
        static {
            System.out.println("[Parent] static block");
        }

        {
            System.out.println("[Parent] instance init block");
        }

        Parent() {
            System.out.println("[Parent] constructor");
        }
    }

    static class Child extends Parent {
        static {
            System.out.println("[Child] static block");
        }

        {
            System.out.println("[Child] instance init block");
        }

        Child() {
            System.out.println("[Child] constructor");
        }
    }
}
