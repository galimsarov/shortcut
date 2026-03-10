package com.example.a_alg_and_structs.a_map_or_set;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Даны 2 массива целых чисел. Найти максимальное число, встречающееся в обоих массивах.
 * Как решал: решил с подсказкой через set.
 * 5 примеров тестовых данных (пары массивов, включая граничные случаи):
 * [1, 2, 3, 10] и [5, 10, 11] → 10
 * [7, 7, 8, 9] и [1, 2, 7] → 7
 * [] и [1, 2, 3] → общего числа нет
 * [4, 5, 6] и [1, 2, 3] → общего числа нет
 * [] и [] → общего числа нет
 */
public class BMaxCommonIntFinder {
    public static void main(String[] args) {
        int[] arr1 = new int[]{};
        int[] arr2 = new int[]{};
        System.out.println(Arrays.toString(arr1));
        System.out.println(Arrays.toString(arr2));
        try {
            int maxCommonInt = getMaxCommonInt(arr1, arr2);
            System.out.println("Max common int value: " + maxCommonInt);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private static int getMaxCommonInt(int[] arr1, int[] arr2) {
        if (arr1.length == 0 || arr2.length == 0) {
            throw new RuntimeException("There is no max common int value");
        }
        int maxCommonInt = Integer.MIN_VALUE;
        boolean valueFound = false;
        Set<Integer> set = new HashSet<>();
        for (int j : arr1) {
            set.add(j);
        }
        for (int j : arr2) {
            if (set.contains(j)) {
                if (maxCommonInt < j) {
                    maxCommonInt = j;
                    valueFound = true;
                }
            }
        }
        if (!valueFound) {
            throw new RuntimeException("There is no max common int value");
        }
        return maxCommonInt;
    }
}
