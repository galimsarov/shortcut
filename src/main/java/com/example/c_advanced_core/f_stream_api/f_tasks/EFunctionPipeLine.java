package com.example.c_advanced_core.f_stream_api.f_tasks;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Построить цепочку обработки входной строки с помощью функций.
 * Требования:
 * Сделать функции:
 * trim,
 * toLowerCase,
 * замена пробелов на _,
 * обрезка до 20 символов.
 * Объединить функции через andThen в единый pipeline.
 * Добавить возможность передавать дополнительный шаг как параметр (Function<String, String>).
 * Показать минимум 3 примера входных строк.
 */
public class EFunctionPipeLine {
    public static void main(String[] args) {
        Function<String, String> trimF = s -> s.trim();
        Function<String, String> lowerCaseF = s -> s.toLowerCase();
        Function<String, String> spaceToUnderscoreF = s -> s.replaceAll(" ", "_");
        Function<String, String> firstTwentyF = s -> {
            if (s.length() <= 20) {
                return s;
            }
            return s.substring(0, 20);
        };
        List<String> words = List.of(
                "   Hello World   ",                     // пробелы по краям
                "Java Stream API Example",              // обычная строка
                "Functional Programming in Java 21",    // длиннее 20 символов
                "SHORT",                                // короткая строка
                "   MULTIPLE   SPACES   HERE   "        // много пробелов
        );
        words.stream().forEach(word -> System.out.println(pipeLine(
                word,
                trimF
        )));
        words.stream().forEach(word -> System.out.println(pipeLine(
                word,
                trimF,
                lowerCaseF
        )));
        words.stream().forEach(word -> System.out.println(pipeLine(
                word,
                trimF,
                lowerCaseF,
                spaceToUnderscoreF,
                firstTwentyF
        )));
    }

    private static String pipeLine(String line, Function<String, String>... functions) {
        // Нужно сделать последовательное применение всех функций из перечисления.
        // Начинаем с line, применяем первую функцию, потом к результату вторую и т.д.
        // Возвращаем результат
        Function<String, String> pipeline =
                Stream.of(functions)
                        .reduce(Function.identity(), Function::andThen);
        return pipeline.apply(line);
    }
}
