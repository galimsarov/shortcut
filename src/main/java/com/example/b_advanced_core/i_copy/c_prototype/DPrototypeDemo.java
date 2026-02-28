package com.example.b_advanced_core.i_copy.c_prototype;

/**
 * Где удобно:
 *
 * когда создание объекта «с нуля» дорогое;
 * когда много готовых пресетов/шаблонов состояния;
 * когда нужно скрыть детали инициализации.
 */
public class DPrototypeDemo {
    public static void main(String[] args) {
        CUnitRegistry registry = new CUnitRegistry();
        registry.register("archer", new BGameUnit("Archer", 70));
        registry.register("tank", new BGameUnit("Tank", 200));

        BGameUnit u1 = registry.create("archer");
        BGameUnit u2 = registry.create("tank");

        System.out.println(u1);
        System.out.println(u2);
    }
}
