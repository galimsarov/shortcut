package com.example.b_core.c_runtime.b_binding;

public class Child extends Parent {
    static void staticMethod() {
        System.out.println("Child.staticMethod");
    }

    @Override
    void instanceMethod() {
        System.out.println("Child.instanceMethod");
    }
}
