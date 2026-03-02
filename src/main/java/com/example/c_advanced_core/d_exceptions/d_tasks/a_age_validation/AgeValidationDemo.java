package com.example.c_advanced_core.d_exceptions.d_tasks.a_age_validation;

/**
 * Сделать метод регистрации, который валидирует возраст.

 * Требования:

 * Метод register(int age):
 * если возраст < 18, бросать собственное checked-исключение UnderAgeException.
 * В main показать обработку через try-catch и понятное сообщение пользователю.
 * Добавить ветку успешной регистрации.
 */

public class AgeValidationDemo {
    public static void main(String[] args) {
        register(17);
        register(18);
    }

    private static void register(int age) {
        try {
            isValid(age);
            System.out.println("User with age: " + age + " was successfully registered");
        } catch (UnderAgeException e) {
            System.out.println("User wasn't registered: " + e.getMessage());
        }
    }

    private static boolean isValid(int age) throws UnderAgeException {
        if (age < 18) {
            throw new UnderAgeException("Age is less than 18");
        }
        return true;
    }
}
