package com.example.c_advanced_core.c_comparable_comparator.c_tasks.b_comparator;

import lombok.Getter;

@Getter
public enum Category {
    A(1),
    B(2),
    C(3),
    D(4);

    private final int value;

    private Category(int value) {
        this.value = value;
    }
}
