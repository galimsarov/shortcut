package com.example.a_alg_and_structs.c_streams.c_employees_grouping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Дан список Employee { String department; BigDecimal salary; }. Построить Map<String, BigDecimal>, где для каждого департамента хранится средняя зарплата сотрудников.
 * 5 примеров тестовых данных:
 * [(IT, 100000), (IT, 140000), (HR, 80000)] → {IT: 120000, HR: 80000}
 * [(Sales, 90000), (Sales, 110000), (Sales, 100000)] → {Sales: 100000}
 * [(QA, 70000), (DevOps, 130000), (QA, 90000), (DevOps, 110000)] → {QA: 80000, DevOps: 120000}
 * [(Finance, 0), (Finance, 100000)] → {Finance: 50000}
 * [] → {}
 */
public class EmployeeGrouper {
    public static void main(String[] args) {
        List<Employee> employees = getEmployees5();
        System.out.println(employees);
        Map<String, BigDecimal> result = new HashMap<>();
        employees.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment))
                .forEach((key, value) -> {
                    double avgSalary = value.stream()
                            .map(employee -> employee.getSalary().intValue())
                            .collect(Collectors.averagingInt(Integer::intValue));
                    result.put(key, new BigDecimal(avgSalary));
                });
        System.out.println(result);
    }

    private static List<Employee> getEmployees1() {
        return new ArrayList<>(List.of(
                new Employee("IT", new BigDecimal("100000")),
                new Employee("IT", new BigDecimal("140000")),
                new Employee("HR", new BigDecimal("80000"))

        ));
    }

    private static List<Employee> getEmployees2() {
        return new ArrayList<>(List.of(
                new Employee("Sales", new BigDecimal("90000")),
                new Employee("Sales", new BigDecimal("110000")),
                new Employee("Sales", new BigDecimal("100000"))

        ));
    }

    private static List<Employee> getEmployees3() {
        return new ArrayList<>(List.of(
                new Employee("QA", new BigDecimal("70000")),
                new Employee("DevOps", new BigDecimal("130000")),
                new Employee("QA", new BigDecimal("90000")),
                new Employee("DevOps", new BigDecimal("110000"))
        ));
    }

    private static List<Employee> getEmployees4() {
        return new ArrayList<>(List.of(
                new Employee("Finance", new BigDecimal("0")),
                new Employee("Finance", new BigDecimal("100000"))
        ));
    }

    private static List<Employee> getEmployees5() {
        return new ArrayList<>();
    }
}
