package com.example.a_alg_and_structs.a_map_or_set;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Дана строка текста. Посчитать количество уникальных слов без учёта регистра и знаков препинания.
 * 5 примеров тестовых данных (включая граничные случаи):
 * "Java, java! Kotlin." → 2 (java, kotlin)
 * "One two three" → 3
 * "Hello... HELLO???" → 1
 * "" → 0 (пустая строка)
 * " " → 0 (строка только из пробелов)
 */
public class EUniqueWordsCounter {
    public static void main(String[] args) {
        String string = "Java, java! Kotlin.";
        System.out.println(string);
        int uniqueWordsCount = countUniqueWords(string);
        System.out.println(uniqueWordsCount);
    }

    private static int countUniqueWords(String str) {
        String modifiedString = str
                .replace(",", "")
                .replace(".", "")
                .replace("!", "")
                .replace("?", "")
                .toLowerCase();
        if (modifiedString.isEmpty()) {
            return 0;
        }
        String[] words = modifiedString.split(" ");
        return Arrays.stream(words).collect(Collectors.toSet()).size();
    }
}
