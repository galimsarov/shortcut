package com.example.c_advanced_core.h_serialization.b_tasks.a_basic;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Сохранение и восстановление профиля
 * Задача: Сериализовать объект UserProfile в файл и прочитать обратно.
 * Требования:
 * - Класс реализует Serializable.
 * - Поля: username, email, age.
 * - После десериализации сравнить поля исходного и восстановленного объекта.
 * - Добавить serialVersionUID.
 */
@Getter
@Setter
@ToString
public class UserProfile implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String email;
    private int age;

    public UserProfile(String username, String email, int age) {
        this.username = username;
        this.email = email;
        this.age = age;
    }
}
