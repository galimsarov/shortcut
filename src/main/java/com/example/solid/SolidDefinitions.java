package com.example.solid;

import java.util.List;

/**
 * Краткие формулировки SOLID (каноничные и упрощённые).
 */
public final class SolidDefinitions {

    private SolidDefinitions() {
    }

    public static List<Definition> all() {
        return List.of(
                new Definition(
                        "SRP — Single Responsibility Principle",
                        "A class should have only one reason to change.",
                        "Один класс = одна зона ответственности."
                ),
                new Definition(
                        "OCP — Open/Closed Principle",
                        "Software entities should be open for extension, but closed for modification.",
                        "Добавляем новое поведение через расширение, а не переписывание существующего кода."
                ),
                new Definition(
                        "LSP — Liskov Substitution Principle",
                        "Objects of a superclass should be replaceable with objects of its subclasses without breaking the correctness of the program.",
                        "Подтип должен полноценно заменять базовый тип без сюрпризов."
                ),
                new Definition(
                        "ISP — Interface Segregation Principle",
                        "Clients should not be forced to depend on methods they do not use.",
                        "Лучше много маленьких интерфейсов, чем один толстый."
                ),
                new Definition(
                        "DIP — Dependency Inversion Principle",
                        "High-level modules should not depend on low-level modules. Both should depend on abstractions.",
                        "Зависим от интерфейсов, а не от конкретных реализаций."
                )
        );
    }

    public record Definition(String name, String canonical, String simplified) {
    }
}
