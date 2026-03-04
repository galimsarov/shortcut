package com.example.c_advanced_core.i_copy.d_tasks.a_shallow_vs_deep;

/**
 * Смоделировать пользователя с вложенным объектом адреса и показать разницу shallow/deep copy.
 * Требования:
 * 1. Классы User и Address.
 * 2. Реализовать:
 * - поверхностную копию,
 * - глубокую копию (через конструктор копирования).
 * 3. После изменения адреса в копии показать поведение оригинала в обоих случаях.
 */
public class CopyDemo {
    public static void main(String[] args) {
        User original = new User("Alex", new Address("Red square", "Moscow", "Moscow"));

        User shallow = original.shallowCopy();
        User deep = original.deepCopy();

        Address address = original.getAddress();
        address.setCity("New York");
        address.setState("New York");
        address.setStreet("Main Street");

        shallow.setName("Shallow");
        System.out.println("shallow: " + shallow);

        deep.setName("Deep");
        System.out.println("deep: " + deep);
    }
}
