package com.example.a_alg_and_structs.b_two_pointers;

import java.util.Arrays;

/**
 * Дан массив целых чисел, в котором могут быть нули.
 * Решить задачу со сложностью O(1) по памяти и O(n) по вычислениям: все нули переместить в конец,
 * все ненулевые значения — в начало, сохранив их порядок.
 * Как решал: после созвона и дополнительного изучения темы двух указателей.
 * 5 примеров тестовых данных (включая граничные случаи):
 * - [0, 1, 0, 3, 12] → [1, 3, 12, 0, 0]
 * - [0, 0, 0] → [0, 0, 0] (массив только из нулей)
 * - [4, 5, 6] → [4, 5, 6] (массив без нулей)
 * - [] → [] (пустой массив)
 * - [0, -1, 2, 0, -3, 0, 4] → [-1, 2, -3, 4, 0, 0, 0]
 */
public class AZeroesMover {
    public static void main(String[] args) {
        int[] arr = new int[]{0, -1, 2, 0, -3, 0, 4};
        System.out.println("Before sorting: " + Arrays.toString(arr));
        moveZeroes(arr);
        System.out.println("After sorting: " + Arrays.toString(arr));
    }

    public static void moveZeroes(int[] arr) {
        if (arr.length == 0) {
            return;
        }
        int readIndex = 0;
        int writeIndex = 0;
        while (readIndex < arr.length) {
            int temp = arr[readIndex];
            if (temp != 0) {
                arr[writeIndex] = temp;
                writeIndex++;
            }
            readIndex++;
        }
        for (int i = writeIndex; i < arr.length; i++) {
            arr[i] = 0;
        }
    }
}
