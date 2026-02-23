package com.example.core.a_solid.c_lsp;

/**
 * LSP: подтип не должен ломать ожидания базового типа.
 */
public class LspExample {

    // ❌ Нарушение LSP: страус формально птица, но летать не может.
    static class Bird {
        void fly() {
            System.out.println("Bird flies");
        }
    }

    static class Ostrich extends Bird {
        @Override
        void fly() {
            throw new UnsupportedOperationException("Ostrich can't fly");
        }
    }

    interface BirdMover {
        void move();
    }

    static class Sparrow implements BirdMover {
        @Override
        public void move() {
            System.out.println("Sparrow flies");
        }
    }

    static class WalkingBird implements BirdMover {
        @Override
        public void move() {
            System.out.println("Walking bird runs");
        }
    }

    // ✅ LSP: все реализации корректно заменяют абстракцию BirdMover.
    static void relocateBird(BirdMover birdMover) {
        birdMover.move();
    }

    public static void demo() {
        relocateBird(new Sparrow());
        relocateBird(new WalkingBird());
    }
}
