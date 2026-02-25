package com.example.b_advanced_core.c_comparable_comparator.a_comparable;

import lombok.Getter;

/**
 * «класс умеет сравнивать сам себя»
 */

@Getter
public class Student implements Comparable<Student> { // Comparable<T> — естественный порядок внутри самого класса
    private final String name;
    private final int score;

    Student(String name, int score) {
        this.name = name;
        this.score = score;
    }

    @Override
    public int compareTo(Student other) {
        // естественный порядок: по score (по возрастанию)
        return Integer.compare(this.score, other.score);
    }

    @Override
    public String toString() {
        return name + "(" + score + ")";
    }
}
