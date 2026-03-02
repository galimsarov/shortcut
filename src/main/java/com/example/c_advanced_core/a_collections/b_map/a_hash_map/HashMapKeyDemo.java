package com.example.c_advanced_core.a_collections.b_map.a_hash_map;

import java.util.HashMap;
import java.util.Map;

/**
 * Как работает внутри (упрощённо)
 * 1. По key.hashCode() вычисляется hash.
 * 2. Hash преобразуется в индекс корзины (bucket).
 * 3. В корзине ищется нужный ключ:
 * 3.1. сначала через сравнение hash,
 * 3.2. затем через equals().
 * 4. При коллизиях элементы живут в одной корзине:
 * 4.1. в новых JDK сначала список,
 * 4.2. при большом числе коллизий может стать деревом (tree bin), чтобы ускорить доступ.
 * Важные свойства
 * - Средняя сложность put/get/remove — O(1), худший случай — O(n) (или O(log n) при tree bin).
 * - Разрешает null ключ (один) и null значения.
 * - Не потокобезопасен.
 * Критично: equals/hashCode
 * Если ключ — ваш класс, корректно переопределяйте оба метода.
 *
 * Частые ошибки с HashMap
 * - Изменяемый ключ (меняете поле, участвующее в hashCode/equals, после вставки).
 * - Плохой hashCode (сильные коллизии).
 * - Использование из нескольких потоков без синхронизации.
 */
public class HashMapKeyDemo {
    public static void main(String[] args) {
        Map<UserId, String> map = new HashMap<>();
        map.put(new UserId(42), "Alice");

        System.out.println(map.get(new UserId(42))); // Alice
    }
}
