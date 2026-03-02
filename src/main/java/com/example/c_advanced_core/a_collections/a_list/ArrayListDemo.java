package com.example.c_advanced_core.a_collections.a_list;

import java.util.ArrayList;
import java.util.List;

/**
 * Внутри — динамический массив.
 * Плюсы
 * - Быстрый доступ по индексу get(i).
 * - Обычно хорошая cache locality.
 * Минусы
 * - Вставка/удаление в середине требует сдвига элементов.
 * - При росте может перевыделять массив.
 */

public class ArrayListDemo {
    public static void main(String[] args) {
        List<String> list = new ArrayList<>(); // 👉 Фактически массив НЕ создаётся сразу.
        // capacity = 0
        list.add("A"); // 📌 При первом добавлении создаётся массив размером 10
        // capacity = 10

        list.add("B");
        list.add("C");

        System.out.println(list.get(1)); // B: O(1)

        list.add(1, "X"); // сдвиг вправо: O(n)
        // Когда размер листа достигает размера массива, который под капотом, происходит увеличение размера массива
        // в 1,5 раза

        // Что происходит внутри?
        // - Создаётся новый массив большего размера
        // - Через System.arraycopy() копируются элементы
        // - Старая ссылка заменяется новой
        // 📌 Операция O(n)

        System.out.println(list); // [A, X, B, C]

        list.remove(2); // удаление со сдвигом: O(n)

        // Массив не уменьшается автоматически, даже если list.clear()
        // Но можно сделать так: list.trimToSize(), но объект, конечно, должен быть не List, а ArrayList

        // 📌 Почему нет авто-уменьшения?
        // Потому что:
        // - это дорого
        // - может часто вызываться
        // вызовет постоянные копирования

        // !!! ArrayList оптимизирован под добавление !!!

        System.out.println(list); // [A, X, C]
    }
}
