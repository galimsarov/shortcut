package com.example.a_alg_and_structs.a_map_or_set;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Дан массив чисел. Определить, есть ли в нём хотя бы одно повторяющееся значение.
 * 5 примеров тестовых данных (включая граничные случаи):
 * [1, 2, 3, 1] → true
 * [5, 6, 7, 8] → false
 * [] → false (пустой массив)
 * [42] → false (один элемент)
 * [-1, -2, -3, -1] → true
 */
public class DDuplicatesInArrayFinder {
    public static void main(String[] args) {
        int[] arr = {-1, -2, -3, -1};
        boolean hasDuplicates = hasDuplicates(arr);
        System.out.println(Arrays.toString(arr));
        System.out.println(hasDuplicates);
    }

    private static boolean hasDuplicates(int[] arr) {
        if (arr.length < 2) {
            return false;
        }
        Set<Integer> set = new HashSet<>();
        for (int j : arr) {
            set.add(j);
        }
        return set.size() != arr.length;
    }

}
