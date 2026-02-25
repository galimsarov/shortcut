package com.example.b_advanced_core.c_comparable_comparator.b_comparator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Comparator: «сортируем по-разному в зависимости от задачи»
 * Когда подходит Comparator:
 * - нужно несколько вариантов сортировки;
 * - не хотите менять исходный класс;
 * - сортируете внешний тип (например, из библиотеки).
 */

public class ComparatorDemo {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>(List.of(
                new Student("Mila", 92, 20),
                new Student("Artem", 81, 19),
                new Student("Ira", 92, 18)
        ));

        // 1) По score по убыванию
//        Comparator<Student> comparator = (Student s1, Student s2) -> Integer.compare(s2.getScore(), s1.getScore());
//        students.sort(comparator);
        students.sort(Comparator.comparingInt(Student::getScore).reversed());

        System.out.println("By score desc: " + students);

        // 2) По name по возрастанию
        students.sort(Comparator.comparing(Student::getName));
        System.out.println("By name asc: " + students);

        // 3) Сложный порядок: score desc, затем age asc
        students.sort(
                Comparator.comparingInt(Student::getScore).reversed()
                        .thenComparingInt(Student::getAge)
        );
        System.out.println("By score desc, age asc: " + students);
    }
}
