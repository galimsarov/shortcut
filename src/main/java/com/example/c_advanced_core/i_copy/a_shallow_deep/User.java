package com.example.c_advanced_core.i_copy.a_shallow_deep;

import java.util.ArrayList;
import java.util.List;

/**
 * Допустим, есть объект User, внутри которого лежит Address.
 * - Shallow copy копирует поля верхнего уровня, но вложенные объекты остаются общими.
 * - Deep copy копирует весь граф объектов (или ту его часть, которую вы считаете частью состояния).
 */
class User {
    String name;
    Address address;
    List<String> tags;

    User(String name, Address address, List<String> tags) {
        this.name = name;
        this.address = address;
        this.tags = tags;
    }

    // Shallow copy: Address и tags будут теми же объектами
    User shallowCopy() {
        return new User(this.name, this.address, this.tags);
    }

    // Deep copy: создаём новые вложенные объекты
    User deepCopy() {
        Address copiedAddress = new Address(this.address.city);
        List<String> copiedTags = new ArrayList<>(this.tags);
        return new User(this.name, copiedAddress, copiedTags);
    }
}
