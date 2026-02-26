package com.example.b_advanced_core.e_generics.b_typed.a_class;

/**
 * Использование:
 */
public class BBoxDemo {
    public static void main(String[] args) {
        ABox<String> stringBox = new ABox<>("Java");
        String text = stringBox.getValue();
        System.out.println(text);

        ABox<Integer> intBox = new ABox<>(42);
        Integer number = intBox.getValue();
        System.out.println(number);
    }
}
