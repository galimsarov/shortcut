package com.example.c_advanced_core.i_copy.b_clone;

import java.util.ArrayList;
import java.util.List;

/**
 * Object#clone() делает поверхностное копирование (по умолчанию) и требует Cloneable.
 * Минусы clone():
 * - неочевидный контракт (Cloneable — маркер без метода);
 * - легко забыть углубить mutable-поля;
 * - неудобно с final полями и иерархиями;
 * - хуже читается, чем явный copy-конструктор/фабрика.
 * Обычно в продакшене чаще используют:
 * - copy constructor (new User(existingUser));
 * - статическую фабрику (User.copyOf(user));
 * - record + неизменяемые поля (копирование через создание нового экземпляра).
 */
class Profile implements Cloneable {
    String name;
    List<String> skills;

    Profile(String name, List<String> skills) {
        this.name = name;
        this.skills = skills;
    }

    @Override
    protected Profile clone() {
        try {
            Profile copy = (Profile) super.clone(); // shallow-копия полей
            copy.skills = new ArrayList<>(this.skills); // вручную углубляем нужные поля
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }
}