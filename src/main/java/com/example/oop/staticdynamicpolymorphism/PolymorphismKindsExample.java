package com.example.oop.staticdynamicpolymorphism;

/**
 * Пакет 2: статический и динамический полиморфизм.
 *
 * Статический (compile-time) полиморфизм в Java:
 * - перегрузка (overload): выбор метода на этапе компиляции по сигнатуре.
 *
 * Динамический (runtime) полиморфизм в Java:
 * - переопределение (override): выбор метода во время выполнения по фактическому типу объекта.
 */
public final class PolymorphismKindsExample {

    private PolymorphismKindsExample() {
    }

    public static void demo() {
        Printer printer = new Printer();

        // Статический полиморфизм: у метода print две сигнатуры.
        System.out.println(printer.print("Привет"));
        System.out.println(printer.print(42));

        // Динамический полиморфизм: ссылка базового типа смотрит на наследника.
        Shape shape1 = new Circle();
        Shape shape2 = new Rectangle();
        System.out.println("shape1 area: " + shape1.area(10, 0));
        System.out.println("shape2 area: " + shape2.area(10, 5));
    }

    static class Printer {
        // Overload #1
        String print(String text) {
            return "String: " + text;
        }

        // Overload #2
        String print(int number) {
            return "Int: " + number;
        }
    }

    static class Shape {
        double area(double a, double b) {
            return 0;
        }
    }

    static class Circle extends Shape {
        @Override
        double area(double radius, double ignored) {
            return Math.PI * radius * radius;
        }
    }

    static class Rectangle extends Shape {
        @Override
        double area(double width, double height) {
            return width * height;
        }
    }
}
