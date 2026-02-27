package com.example.b_advanced_core.f_stream_api.e_collect;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record Employee(String name, String department, int salary) {}

/**
 * collect: группировка и агрегация
 * в collect подаём Collectors с каким либо статическим методом
 */
public class CollectorsDemo {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Ann", "IT", 2000),
                new Employee("Bob", "IT", 2500),
                new Employee("Kate", "HR", 1800)
        );

        Map<String, List<Employee>> byDepartment = employees.stream()
                .collect(Collectors.groupingBy(Employee::department));

        Map<String, Integer> salaryByName = employees.stream()
                .collect(Collectors.toMap(Employee::name, Employee::salary));

        double avgSalary = employees.stream()
                .collect(Collectors.averagingInt(Employee::salary));

        System.out.println(byDepartment);
        System.out.println(salaryByName);
        System.out.println(avgSalary);
    }
}
