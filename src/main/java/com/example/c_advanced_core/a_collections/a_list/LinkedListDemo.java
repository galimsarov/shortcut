package com.example.c_advanced_core.a_collections.a_list;

import java.util.LinkedList;
import java.util.List;

/**
 * Двусвязный список (prev/next ссылки).
 * Плюсы
 * - Быстрые операции на концах (addFirst/addLast/removeFirst/removeLast) — O(1).
 * - Реализует и List, и Deque.
 * Минусы
 * - Доступ по индексу медленный (линейный обход).
 * - Памяти тратится больше (на узлы и ссылки).
 *
 * LinkedList имеет смысл, если:
 * ✅ Часто:
 * - вставки/удаления в начале
 * - используешь как очередь / deque
 * ✅ Нужна реализация:
 * - Deque
 * - Queue
 */
public class LinkedListDemo {
    public static void main(String[] args) {
        List<Integer> deque = new LinkedList<>();
        deque.addFirst(10);
        deque.addLast(20);
        deque.addLast(30);

        System.out.println(deque); // [10, 20, 30]
        System.out.println(deque.removeFirst()); // 10
        System.out.println(deque.get(1)); // 30, но get(i) ~ O(n)
    }
}
