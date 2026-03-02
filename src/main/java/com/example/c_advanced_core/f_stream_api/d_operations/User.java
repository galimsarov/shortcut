package com.example.c_advanced_core.f_stream_api.d_operations;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class User {
    private final String name;
    private final int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
