package com.example.c_advanced_core.c_comparable_comparator.a_comparable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Когда подходит Comparable:
 * - у сущности есть один «дефолтный» порядок (например, LocalDate, String, Integer);
 * - этот порядок логично сделать частью модели.
 */

public class ComparableDemo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
                new Student("Mila", 92),
                new Student("Artem", 81),
                new Student("Ira", 92)
        ));

        Collections.sort(students); // использует compareTo
        System.out.println(students); // [Artem(81), Mila(92), Ira(92)]
    }
}
