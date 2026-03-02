package com.example.c_advanced_core.f_stream_api.d_operations;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class OperationsDemo {
    public static void main(String[] args) {
        filterDemo();
        mapDemo();
        flatMapDemo();
        otherIntermediateOperationsDemo();
        otherTerminalOperationsDemo();
    }

    /**
     * filter — отбор
     */
    private static void filterDemo() {
        List<User> users = List.of(
                new User("Alice", 18),
                new User("Bob", 17),
                new User("Charlie", 19)
        );
        List<String> adults = users.stream()
                .filter(u -> u.getAge() >= 18)
                .map(User::getName)
                .toList();
        System.out.println(adults);
    }

    /**
     * map — преобразование 1 к 1
     */
    private static void mapDemo() {
        List<Integer> lengths = Stream.of("java", "stream", "api")
                .map(String::length)
                .toList(); // [4, 6, 3]
        System.out.println(lengths);
    }

    /**
     * flatMap — «расплющивание» вложенных структур
     */
    private static void flatMapDemo() {
        List<List<String>> lines = List.of(
                List.of("a", "b"),
                List.of("c"),
                List.of("d", "e")
        );
        List<String> all = lines.stream()
                .flatMap(Collection::stream)
                .toList(); // [a, b, c, d, e]
        System.out.println(all);
    }

    /**
     * distinct, sorted, limit, skip
     */
    private static void otherIntermediateOperationsDemo() {
        List<Integer> top3 = Stream.of(7, 1, 3, 3, 9, 2, 9, 10)
                .distinct()                  // убрали дубликаты
                .sorted()                    // [1, 2, 3, 7, 9, 10]
                .skip(1)                     // [2, 3, 7, 9, 10]
                .limit(3)                    // [2, 3, 7]
                .toList();
        System.out.println(top3);
    }

    /**
     * Терминальные операции: forEach, count, findFirst, reduce, collect
     */
    private static void otherTerminalOperationsDemo() {
        long cnt = List.of("a", "bb", "ccc").stream()
                .filter(s -> s.length() >= 2)
                .count(); // 2
        System.out.println(cnt);
        int sum = Stream.of(1, 2, 3, 4)
                .reduce(0, Integer::sum); // 10
        System.out.println(sum);
    }
}
