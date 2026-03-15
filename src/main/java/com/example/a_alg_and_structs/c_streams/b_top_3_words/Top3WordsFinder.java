package com.example.a_alg_and_structs.c_streams.b_top_3_words;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Топ-3 самых частых слов в тексте (streams)
 * Дан список строк. Нужно получить 3 наиболее часто встречающихся слова (без учёта регистра и знаков препинания),
 * отсортировав по убыванию частоты, а при равной частоте — по алфавиту.
 * 5 примеров тестовых данных:
 * ["Java Java Kotlin", "kotlin, java!", "Scala"] → ["java", "kotlin", "scala"]
 * ["a b c", "a b", "a"] → ["a", "b", "c"]
 * ["cat dog cat", "dog dog", "tiger"] → ["dog", "cat", "tiger"]
 * ["one, two, two", "three three", "one"] → ["one", "three", "two"] (у всех частота 2, сортировка по алфавиту)
 * [] → []
 */
public class Top3WordsFinder {
    public static void main(String[] args) {
//        List<String> words = Arrays.asList("Java Java Kotlin", "kotlin, java!", "Scala");
//        List<String> words = Arrays.asList("a b c", "a b", "a");
//        List<String> words = Arrays.asList("cat dog cat", "dog dog", "tiger");
//        List<String> words = Arrays.asList("one, two, two", "three three", "one");
        List<String> words = List.of();

        System.out.println(words);
        Map<String, Long> wordsMap = words
                .stream()
                .map(str ->
                        str.toLowerCase()
                                .replace(",", "")
                                .replace("!", "")
                ).map(str -> str.split(" "))
                .flatMap(Arrays::stream)
                .collect(Collectors.groupingBy(s -> s, Collectors.counting()));
        System.out.println(wordsMap);
        List<String> sortedWords = wordsMap.entrySet().stream()
                .sorted((o1, o2) -> {
                    long diff = o2.getValue() - o1.getValue();
                    if (diff == 0) {
                        return o1.getKey().compareTo(o2.getKey());
                    } else {
                        return (int) diff;
                    }
                }).map(Map.Entry::getKey)
                .toList();
        System.out.println(sortedWords);
    }
}
