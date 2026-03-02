package com.example.b_core.b_oop;

import com.example.b_core.b_oop.c_constructororder.ConstructorOrderExample;
import com.example.b_core.b_oop.a_encapsulationinheritancepolymorphism.BasicOopExample;
import com.example.b_core.b_oop.d_methodbehavior.MethodBehaviorExample;
import com.example.b_core.b_oop.b_staticdynamicpolymorphism.PolymorphismKindsExample;

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
