package com.example.c_advanced_core.c_comparable_comparator.c_tasks.a_comparable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
                new Student(1, "Charlie", 4.5),
                new Student(2, "Bill", 4),
                new Student(3, "Alex", 4.5)
        ));
        System.out.println("Before sorting: " + students);
        Collections.sort(students);
        System.out.println("After sorting: " + students);
    }
}
