package com.example.oop;

import com.example.oop.constructororder.ConstructorOrderExample;
import com.example.oop.encapsulationinheritancepolymorphism.BasicOopExample;
import com.example.oop.methodbehavior.MethodBehaviorExample;
import com.example.oop.staticdynamicpolymorphism.PolymorphismKindsExample;

/**
 * Точка входа для запуска всех примеров по ООП в Java.
 */
public class OopDemoApp {
    public static void main(String[] args) {
        System.out.println("=== 1) Инкапсуляция, наследование, полиморфизм ===");
        BasicOopExample.demo();

        System.out.println("\n=== 2) Статический и динамический полиморфизм ===");
        PolymorphismKindsExample.demo();

        System.out.println("\n=== 3) Порядок вызова конструкторов и статических блоков ===");
        ConstructorOrderExample.demo();

        System.out.println("\n=== 4) Обычные и статические методы при наследовании ===");
        MethodBehaviorExample.demo();
    }
}
