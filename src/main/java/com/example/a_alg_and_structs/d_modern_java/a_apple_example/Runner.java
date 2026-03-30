package com.example.a_alg_and_structs.d_modern_java.a_apple_example;

import com.example.a_alg_and_structs.d_modern_java.a_apple_example.pretty_printer.AppleFancyFormatter;
import com.example.a_alg_and_structs.d_modern_java.a_apple_example.pretty_printer.AppleFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class Runner {
    public static void main(String[] args) {
        List<Apple> inventory = new ArrayList<>(List.of(
                new Apple(Color.GREEN, 160),
                new Apple(Color.RED, 160),
                new Apple(Color.GREEN, 140),
                new Apple(Color.RED, 140)
        ));
        prettyPrintApple(inventory, new AppleFancyFormatter());
    }

    private static void prettyPrintApple(List<Apple> inventory, AppleFormatter formatter) {
        for (final Apple apple : inventory) {
            String output = formatter.accept(apple);
            System.out.println(output);
        }
    }

    private static List<Apple> filterApples(
            final List<Apple> apples, final Predicate<Apple> p
    ) {
        List<Apple> result = new ArrayList<>();
        for (Apple apple : apples) {
            if (p.test(apple)) {
                result.add(apple);
            }
        }
        return result;
    }

    private static boolean isGreenApple(Apple apple) {
        return apple.getColor() == Color.GREEN;
    }

    private static boolean isHeavyApple(Apple apple) {
        return apple.getWeight() > 150;
    }
}
