package com.example.c_advanced_core.f_stream_api.f_tasks;

import java.util.List;
import java.util.stream.Stream;

/**
 * Для списка слов получить агрегированную статистику через Stream API.
 *
 * Требования:
 *
 * Отфильтровать слова длиной меньше 4.
 * Привести к нижнему регистру.
 * Удалить дубликаты.
 * Отсортировать по алфавиту.
 * Собрать в List<String> и вывести результат.
 */
public class AStringsAnalysis {
    public static void main(String[] args) {
        List<String> stringList = Stream.of("Camel", "Frozen", "apple", "Bug", "apple", "Apple")
                .filter(s -> s.length() >= 4)
                .map(String::toLowerCase)
                .distinct()
                .sorted()
                .toList();
        System.out.println(stringList);
    }
}
