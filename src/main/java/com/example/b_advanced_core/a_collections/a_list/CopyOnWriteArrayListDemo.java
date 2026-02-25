package com.example.b_advanced_core.a_collections.a_list;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Потокобезопасная коллекция из java.util.concurrent. При каждой модификации (add/remove/set) создаёт новую копию массива.
 * 👉 массив хранится в volatile поле
 * Плюсы
 * - Итерация без ConcurrentModificationException.
 * - Отлично для «много чтений, мало записей».
 * Минусы
 * - Запись дорогая (копирование всего массива).
 * - Не подходит для частых изменений.
 */

public class CopyOnWriteArrayListDemo {
    public static void main(String[] args) {
        List<String> listeners = new CopyOnWriteArrayList<>();
//        List<String> listeners = new ArrayList<>(); // ConcurrentModificationException
        listeners.add("L1");
        listeners.add("L2");

        for (String listener : listeners) {
            System.out.println(listener);
            // безопасно: не ломает текущую итерацию
            listeners.add("new-" + listener);
        }

        System.out.println(listeners);
    }
}
