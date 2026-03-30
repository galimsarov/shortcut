package com.example.a_alg_and_structs.c_streams.c_employees_grouping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
public class Employee {
    private String department;
    private BigDecimal salary;
}
