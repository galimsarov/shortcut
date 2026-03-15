package com.example.a_alg_and_structs.a_map_or_set;

import java.util.*;

/**
 * Первый неповторяющийся символ в строке
 * Дана строка. Найти первый символ, который встречается ровно один раз. Если такого нет — вернуть специальное значение.
 * 5 примеров тестовых данных (включая граничные случаи):
 * "leetcode" → 'l'
 * "aabbccd" → 'd'
 * "aabbcc" → специальное значение (например, null или -1)
 * "x" → 'x' (строка из одного символа)
 * "" → специальное значение (пустая строка)
 */
public class CFirstUniqueSymbolFinder {
    public static void main(String[] args) {
//        String str = "leetcode";
//        String str = "aabbccd";
//        String str = "aabbcc";
//        String str = "x";
        String str = "";

        System.out.println(str);
        try {
            char c = getFirstUniqueSymbol(str);
            System.out.println(c);
        } catch (IllegalArgumentException e) {
            System.out.println(-1);
        }
    }

    private static char getFirstUniqueSymbol(String str) {
        Map<Character, Integer[]> map = new HashMap<>();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (map.containsKey(c)) {
                Integer[] arr = map.get(c);
                arr[1] = arr[1] + 1;
                map.put(c, arr);
            } else {
                map.put(c, new Integer[]{i, 1});
            }
        }
        Optional<Map.Entry<Character, Integer[]>> optional = map.entrySet().stream()
                .filter(entry -> entry.getValue()[1] == 1)
                .min(Comparator.comparingInt(o -> o.getValue()[0]));
        if (optional.isPresent()) {
            return optional.get().getKey();
        }
        throw new IllegalArgumentException();
    }
}
