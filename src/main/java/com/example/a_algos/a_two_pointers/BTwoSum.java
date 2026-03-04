package com.example.a_algos.a_two_pointers;

import java.util.Arrays;

/**
 * 🔥 Задача 1 — Two Sum в отсортированном массиве
 * Дано: отсортированный массив int[] arr и число target.
 * Нужно вернуть индексы двух чисел, сумма которых равна target.
 * Пример
 * arr = [1, 2, 3, 4, 6, 8]
 * target = 10
 * Ответ: 1 и 5 (2 + 8)
 * Ограничения
 * - O(n) по времени
 * - O(1) по памяти
 * - массив уже отсортирован
 * 📌 Подсказка: указатели навстречу друг другу.
 */

public class BTwoSum {
    public static void main(String[] args) {
        int[] arr = new int[]{1, 2, 3, 4, 6, 8};
//        int target = 10;
//        int target = 8;
//        int target = 7;
//        int target = 6;
//        int target = 5;
//        int target = 4;
//        int target = 3;
//        int target = 9;
//        int target = 11;
//        int target = 12;
        int target = 14;
        int[] result = getSumIndex(arr, target);
        System.out.println(Arrays.toString(arr));
        System.out.println("target = " + target);
        System.out.println(Arrays.toString(result));
        System.out.println("result = " + (arr[result[0]] + arr[result[1]]));
    }

    private static int[] getSumIndex(int[] arr, int target) {
        int left = 0;
        int right = arr.length - 1;
        while (left < right) {
            int sum = arr[left] + arr[right];
            if (sum == target) {
                return new int[]{left, right};
            }
            if (sum < target) {
                left++;
            }
            if (sum > target) {
                right--;
            }
        }
        return new int[]{left, right};
    }
}
