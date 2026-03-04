package com.example.a_algos.a_two_pointers;

import java.util.Arrays;
import java.util.Objects;

/**
 * Удалить дубликаты из отсортированного массива
 * Дано: отсортированный массив.
 * Нужно удалить дубликаты на месте и вернуть новую длину.
 * Пример
 * [1,1,2,2,3,3,3,4]
 * → [1,2,3,4, ?, ?, ?, ?]
 * вернуть 4
 * Ограничения
 * - O(n)
 * - O(1)
 * - без доп. массива
 * 📌 Подсказка: один указатель читает, второй пишет.
 */
public class DDuplicateRemover {
    public static void main(String[] args) {
        Integer[] arr = new Integer[]{1, 1, 2, 2, 3, 3, 3, 4};
        System.out.println(Arrays.toString(arr));
        removeDuplicates(arr);
        System.out.println(Arrays.toString(arr));
        System.out.println(
                Arrays.stream(arr)
                        .filter(Objects::nonNull)
                        .count()
        );
    }

    private static void removeDuplicates(Integer[] arr) {
        if (arr.length < 2) {
            return;
        }
        int readIndex = 1;
        int writeIndex = 0;
        int value = arr[0];
        while (readIndex < arr.length) {
            int tmp = arr[readIndex];
            if (tmp != value) {
                writeIndex++;
                arr[writeIndex] = tmp;
                value = tmp;
            }
            readIndex++;
        }
        for (int i = writeIndex + 1; i < arr.length; i++) {
            arr[i] = null;
        }
    }
}
