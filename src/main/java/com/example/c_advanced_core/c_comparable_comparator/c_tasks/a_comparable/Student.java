package com.example.c_advanced_core.c_comparable_comparator.c_tasks.a_comparable;

import lombok.Getter;

/**
 * Задача: Создать класс Student и реализовать естественный порядок сортировки.

 * Требования:

 * Поля: id, name, gpa.
 * Реализовать Comparable<Student>:
 * сначала по gpa (по убыванию),
 * при равенстве — по name (по возрастанию).
 * Продемонстрировать сортировку списка через Collections.sort.
 */
@Getter
public class Student implements Comparable<Student> {
    private final int id;
    private final String name;
    private final double gpa;

    public Student(int id, String name, double gpa) {
        this.id = id;
        this.name = name;
        this.gpa = gpa;
    }

    @Override
    public int compareTo(Student o) {
        if (this.gpa < o.gpa) {
            return 1;
        } else if (this.gpa > o.gpa) {
            return -1;
        } else {
            return String.CASE_INSENSITIVE_ORDER.compare(this.name, o.name);
        }
    }

    @Override
    public String toString() {
        return "Student [id=" + id + ", name=" + name + ", gpa=" + gpa + "]";
    }
}
