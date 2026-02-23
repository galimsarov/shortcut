package com.example.core.b_oop.d_methodbehavior;

/**
 * Пакет 4: поведение обычных и статических методов в наследовании.
 *
 * Обычные (instance) методы:
 * - переопределяются и вызываются полиморфно (runtime dispatch).
 *
 * Статические методы:
 * - не переопределяются, а скрываются (method hiding),
 *   выбор идёт по типу ссылки на этапе компиляции.
 */
public final class MethodBehaviorExample {

    private MethodBehaviorExample() {
    }

    public static void demo() {
        Base base = new Base();
        Base baseRefToChild = new Child();
        Child child = new Child();

        System.out.println("Common methods:");
        System.out.println("base.whoAmI() -> " + base.whoAmI());
        System.out.println("baseRefToChild.whoAmI() -> " + baseRefToChild.whoAmI());
        System.out.println("child.whoAmI() -> " + child.whoAmI());

        System.out.println("\nStatic methods:");
        // Важно: выбор происходит по ТИПУ ССЫЛКИ, а не объекта.
        System.out.println("Base.role() -> " + Base.role());
        System.out.println("Child.role() -> " + Child.role());
        System.out.println("baseRefToChild.role() -> " + baseRefToChild.role());
    }

    static class Base {
        String whoAmI() {
            return "Base instance method";
        }

        static String role() {
            return "Base static method";
        }
    }

    static class Child extends Base {
        @Override
        String whoAmI() {
            return "Child instance method";
        }

        // Это не override, а hiding.
        static String role() {
            return "Child static method";
        }
    }
}
