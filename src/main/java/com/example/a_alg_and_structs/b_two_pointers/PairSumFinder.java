package com.example.a_alg_and_structs.b_two_pointers;

import java.util.*;

/**
 * Сумма пары в отсортированном массиве
 * Дан отсортированный массив и число target. Найти индексы двух элементов, сумма которых равна target.
 * 5 примеров тестовых данных (включая граничные случаи):
 * nums=[2, 7, 11, 15], target=9 → [0, 1]
 * nums=[1, 2, 3, 4, 6], target=10 → [3, 4]
 * nums=[-5, -2, 0, 3, 9], target=1 → [1, 3]
 * nums=[1, 1], target=2 → [0, 1] (минимальная длина массива)
 * nums=[1, 2, 3], target=100 → пары нет (например, [-1, -1])
 */
public class PairSumFinder {
    public static void main(String[] args) {
//        int[] nums = new int[]{2, 7, 11, 15};
//        int target = 9;

//        int[] nums = new int[]{1, 2, 3, 4, 6};
//        int target = 10;

        int[] nums = new int[]{-5, -2, 0, 3, 9};
        int target = 1;

//        int[] nums = new int[]{1, 1};
//        int target = 2;

//        int[] nums = new int[]{1, 2, 3};
//        int target = 100;

        System.out.println(Arrays.toString(nums));
        System.out.println(target);
        int[] pairIndexes = getPairIndexes(nums, target);
        System.out.println(Arrays.toString(pairIndexes));
    }

    private static int[] getPairIndexes(int[] nums, int target) {
        // Делаем мапу:     Для каждого ключа ищем дополнение до таргета.
        // {2, 0}           У нас есть 2, нужно найти 7, в значениях находим 0 и 1
        // {7, 1}
        // {11, 2}
        // {15, 3}

        // Делаем мапу:     Для каждого ключа ищем дополнение до таргета.
        // {1, 0}           У нас есть 1, ключа 9 нет
        // {2, 1}           У нас есть 2, ключа 8 нет
        // {3, 2}           У нас есть 3, ключа 7 нет
        // {4, 3}           У нас есть 4, нужно найти 6, в значениях находим 3 и 4
        // {6, 4}

        Map<Integer, List<Integer>> indexesByValues = new HashMap<>();
        for (int i = 0; i < nums.length; i++) {
            List<Integer> indexes = indexesByValues.get(nums[i]);
            if (indexes == null) {
                indexes = new ArrayList<>();
            }
            indexes.add(i);
            indexesByValues.put(nums[i], indexes);
        }
        for (Map.Entry<Integer, List<Integer>> entry : indexesByValues.entrySet()) {
            int value = entry.getKey();
            int anotherValue = target - value;
            List<Integer> indexes = indexesByValues.get(anotherValue);
            if (indexes != null) {
                if (indexes.size() > 1 && value * 2 == target) {
                    int[] result = new int[]{indexes.get(0), indexes.get(1)};
                    Arrays.sort(result);
                    return result;
                } else {
                    int index = indexes.get(0);
                    if (index < entry.getValue().get(0)) {
                        return new int[]{index, entry.getValue().get(0)};
                    } else {
                        return new int[]{entry.getValue().get(0), index};
                    }
                }
            }
        }
        return new int[]{-1, -1};
    }
}
