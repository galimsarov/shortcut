package com.example.c_advanced_core.f_stream_api.a_core;

import java.util.List;

/**
 * Stream API — это декларативный способ обработки данных: вы описываете что сделать с набором данных,
 * а не как вручную пройти цикл.
 */
public class StreamDemo {
    /**
     * Ключевые идеи:
     * - Stream не хранит данные сам по себе — он работает поверх источника
     *      (Collection, массив, Files.lines(...), генераторы и т.д.).
     * - Промежуточные операции (map, filter, sorted, distinct, limit...) ленивые.
     * - Вычисление стартует только на терминальной операции
     *      (toList, collect, forEach, count, reduce, findFirst...).
     * - Один stream можно потребить только один раз.
     * @param args
     */
    public static void main(String[] args) {
        List<String> names = List.of("John", "Jane", "Jack", "Jill", "Bob");
        List<String> result = names.stream()         // источник
                .filter(s -> s.length() >= 4)        // промежуточная операция
                .map(String::toUpperCase)            // промежуточная операция
                .sorted()                            // промежуточная операция
                .toList();                           // терминальная операция
        System.out.println(result);
    }
}
