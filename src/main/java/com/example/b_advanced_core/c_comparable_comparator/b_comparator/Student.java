package com.example.b_advanced_core.c_comparable_comparator.b_comparator;

import lombok.Getter;

@Getter
public class Student {
    private final String name;
    private final int score;
    private final int age;

    Student(String name, int score, int age) {
        this.name = name;
        this.score = score;
        this.age = age;
    }

    @Override
    public String toString() {
        return name + "(" + score + ", " + age + ")";
    }
}
