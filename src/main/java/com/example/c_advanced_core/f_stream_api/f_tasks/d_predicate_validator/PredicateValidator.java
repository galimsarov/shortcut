package com.example.c_advanced_core.f_stream_api.f_tasks.d_predicate_validator;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Собрать мини-валидацию User через композицию предикатов.
 * Требования:
 * 1. Класс User: name, email, age.
 * 2. Реализовать набор предикатов:
 * - имя не пустое,
 * - email содержит @,
 * - возраст >= 18.
 * 3. Собрать итоговый валидатор через and.
 * 4. Добавить метод validate(User user, Predicate<User> validator) и вывести понятный результат в консоль.
 */
public class PredicateValidator {
    public static void main(String[] args) {
        Predicate<User> nameValidator = (user) -> !user.getName().isEmpty();
        Predicate<User> emailValidator = (user) -> {
            String email = user.getEmail();
            return email.contains("@");
        };
        Predicate<User> ageValidator = (user) -> {
            int age = user.getAge();
            return age >= 18;
        };
        List<User> users = List.of(
                new User("Alex", "alex@gmail.com", 18),
                new User("", "bob@gmail.com", 18),
                new User("Carl", "carlgmail.com", 18),
                new User("Dan", "dan@gmail.com", 17)
        );
        users.forEach(user -> validate(
                user,
                nameValidator.and(emailValidator).and(ageValidator)
        ));
    }

    private static void validate(User user, Predicate<User> validator) {
        if (validator.test(user)) {
            System.out.println(user + " is valid");
        } else {
            System.out.println(user + " is not valid");
        }
    }
}
