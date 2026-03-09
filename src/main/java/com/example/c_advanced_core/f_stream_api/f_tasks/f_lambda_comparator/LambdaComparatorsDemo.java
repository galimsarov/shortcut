package com.example.c_advanced_core.f_stream_api.f_tasks.f_lambda_comparator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Для списка Employee реализовать несколько вариантов сортировки только лямбдами.
 * Требования:
 * 1. Поля Employee: id, name, department, salary.
 * 2. Отсортировать список:
 * - по salary по убыванию,
 * - по department, затем по name,
 * - по длине имени.
 * 3. Для каждого варианта сортировки вывести результат до/после.
 * 4. Не создавать отдельные именованные классы-компараторы.
 */
public class LambdaComparatorsDemo {
    public static void main(String[] args) {

        Map<String, String> map = new HashMap<>();

        List<Employee> employees = new ArrayList<>(List.of(
                new Employee(1, "Alice", "IT", 120_000),
                new Employee(2, "Bob", "HR", 80_000),
                new Employee(3, "Charlie", "IT", 150_000),
                new Employee(4, "David", "Finance", 120_000),
                new Employee(5, "Eve", "HR", 95_000),
                new Employee(6, "Frank", "Finance", 150_000),
                new Employee(7, "Greg", "IT", 80_000),
                new Employee(8, "Ann", "IT", 120_000),
                new Employee(9, "Christopher", "HR", 110_000)
        ));
        sortBySalaryDesc(employees);
        sortByDepartmentAndName(employees);
        sortByNameLength(employees);
    }

    private static void sortByNameLength(List<Employee> employees) {
        System.out.println("Before sorting by name length: " + employees);
        employees.sort((o1, o2) ->
                Integer.compare(o1.getName().length(), o2.getName().length())
        );
        System.out.println("After sorting by name length: " + employees);
    }

    private static void sortByDepartmentAndName(List<Employee> employees) {
        System.out.println("Before sorting by department and name: " + employees);
        employees.sort((e1, e2) -> {
            int departmentComparingResult = e1.getDepartment().compareTo(e2.getDepartment());
            if (departmentComparingResult == 0) {
                return e1.getName().compareTo(e2.getName());
            }
            return departmentComparingResult;
        });
        System.out.println("After sorting: " + employees);
    }

    private static void sortBySalaryDesc(List<Employee> employees) {
        System.out.println("Before sorting by salary desc: " + employees);
        employees.sort((o1, o2) -> Double.compare(o2.getSalary(), o1.getSalary()));
        System.out.println("After sorting: " + employees);
    }
}
